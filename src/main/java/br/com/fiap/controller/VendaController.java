package br.com.fiap.controller;

import br.com.fiap.assembler.VendaAssembler;
import br.com.fiap.entity.*;
import br.com.fiap.repository.ClienteRepository;
import br.com.fiap.repository.MovimentacaoEstoqueRepository;
import br.com.fiap.repository.ProdutoRepository;
import br.com.fiap.repository.VendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/vendas")
public class VendaController {

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    @Autowired
    private VendaAssembler vendaAssembler;

    // 🔹 Listar todas
    @GetMapping
    public CollectionModel<EntityModel<Venda>> listarTodas() {
        List<EntityModel<Venda>> vendas = vendaRepository.findAll()
                .stream()
                .map(vendaAssembler::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(vendas,
                linkTo(methodOn(VendaController.class).listarTodas()).withSelfRel());
    }

    // 🔹 Buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Venda>> buscarPorId(@PathVariable Long id) {
        return vendaRepository.findById(id)
                .map(vendaAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 🔹 Criar
    @PostMapping
    @Transactional
    public ResponseEntity<EntityModel<Venda>> criar(@RequestBody VendaRequest request) {
        Optional<Cliente> optCliente = clienteRepository.findById(request.clienteId());
        if (optCliente.isEmpty()) return ResponseEntity.notFound().build();
        Cliente cliente = optCliente.get();

        if (request.itens() == null || request.itens().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Map<Long, Produto> cacheProdutos = new HashMap<>();
        double subtotal = 0.0;

        for (ItemVendaDTO item : request.itens()) {
            Produto produto = cacheProdutos.computeIfAbsent(item.produtoId(), id ->
                    produtoRepository.findById(id).orElse(null)
            );
            if (produto == null) return ResponseEntity.notFound().build();

            if (item.quantidade() == null || item.quantidade() <= 0) {
                return ResponseEntity.badRequest().build();
            }

            if (produto.getQuantidadeEstoque() < item.quantidade()) {
                return ResponseEntity.badRequest().build();
            }

            subtotal += produto.getPreco() * item.quantidade();
        }

        double desconto = (subtotal >= 200.0) ? 15.0 : 0.0;
        double totalFinal = subtotal - desconto;

        Venda venda = new Venda();
        venda.setCliente(cliente);
        venda.setDataVenda(LocalDate.now());
        venda.setDesconto(desconto);
        venda.setValorTotal(totalFinal);
        venda.setProdutos(new ArrayList<>(cacheProdutos.values()));

        Venda salva = vendaRepository.save(venda);

        // baixa de estoque
        for (ItemVendaDTO item : request.itens()) {
            Produto produto = cacheProdutos.get(item.produtoId());
            produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - item.quantidade());
            produtoRepository.save(produto);

            MovimentacaoEstoque mov = new MovimentacaoEstoque();
            mov.setProduto(produto);
            mov.setTipo(TipoMovimento.SAIDA);
            mov.setQuantidade(item.quantidade());
            mov.setDataMovimento(LocalDateTime.now());
            mov.setObservacao("Saída por venda ID " + salva.getId());
            movimentacaoRepository.save(mov);
        }

        return ResponseEntity.ok(vendaAssembler.toModel(salva));
    }

    // DTOs
    public record ItemVendaDTO(Long produtoId, Integer quantidade) {}
    public record VendaRequest(Long clienteId, List<ItemVendaDTO> itens) {}
}
