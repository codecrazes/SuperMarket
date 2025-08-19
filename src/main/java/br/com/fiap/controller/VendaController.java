package br.com.fiap.controller;

import br.com.fiap.assembler.VendaAssembler;
import br.com.fiap.entity.Cliente;
import br.com.fiap.entity.Venda;
import br.com.fiap.repository.ClienteRepository;
import br.com.fiap.repository.VendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
    private VendaAssembler vendaAssembler;

    @GetMapping
    public CollectionModel<EntityModel<Venda>> listarTodas() {
        List<EntityModel<Venda>> vendas = vendaRepository.findAll()
                .stream()
                .map(vendaAssembler::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(vendas,
                linkTo(methodOn(VendaController.class).listarTodas()).withSelfRel());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Venda>> buscarPorId(@PathVariable Long id) {
        return vendaRepository.findById(id)
                .map(vendaAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<EntityModel<Venda>> criar(@RequestBody VendaRequest request) {
        Optional<Cliente> optCliente = clienteRepository.findById(request.clienteId());
        if (optCliente.isEmpty()) return ResponseEntity.notFound().build();
        Cliente cliente = optCliente.get();

        Venda venda = new Venda();
        venda.setCliente(cliente);
        venda.setDataVenda(LocalDate.now());
        venda.setDesconto(request.desconto() != null ? request.desconto() : 0.0);
        venda.setValorTotal(request.valorTotal() != null ? request.valorTotal() : 0.0);

        Venda salva = vendaRepository.save(venda);
        return ResponseEntity.ok(vendaAssembler.toModel(salva));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        Optional<Venda> optVenda = vendaRepository.findById(id);
        if (optVenda.isEmpty()) return ResponseEntity.notFound().build();

        vendaRepository.delete(optVenda.get());
        return ResponseEntity.noContent().build();
    }

    // DTO
    public record VendaRequest(Long clienteId, Double valorTotal, Double desconto) {}
}
