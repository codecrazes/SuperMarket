package br.com.fiap.service;

import br.com.fiap.entity.Cliente;
import br.com.fiap.entity.MovimentacaoEstoque;
import br.com.fiap.entity.Produto;
import br.com.fiap.entity.TipoMovimento; 
import br.com.fiap.entity.Venda;
import br.com.fiap.repository.ClienteRepository;
import br.com.fiap.repository.MovimentacaoEstoqueRepository;
import br.com.fiap.repository.ProdutoRepository;
import br.com.fiap.repository.VendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class VendaService {

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    public List<Venda> listarTodas() {
        return vendaRepository.findAll();
    }

    /**
     * Registra uma venda.
     * @param clienteId id do cliente
     * @param itens mapa produtoId -> quantidade
     */
    @Transactional
    public Venda registrarVenda(Long clienteId, Map<Long, Integer> itens) {
        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("A venda precisa ter ao menos 1 item.");
        }

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        double subtotal = 0.0;

        // Carregar produtos e validar estoque
        Map<Long, Produto> produtosCache = new HashMap<>();
        for (Map.Entry<Long, Integer> e : itens.entrySet()) {
            Long produtoId = e.getKey();
            int qtd = e.getValue() == null ? 0 : e.getValue();
            if (qtd <= 0) throw new IllegalArgumentException("Quantidade inválida para o produto " + produtoId);

            Produto produto = produtosCache.computeIfAbsent(produtoId, id ->
                    produtoRepository.findById(id).orElse(null));
            if (produto == null) throw new RuntimeException("Produto id " + produtoId + " não encontrado");

            if (produto.getQuantidadeEstoque() < qtd) {
                throw new RuntimeException("Estoque insuficiente para o produto " + produto.getNome());
            }

            subtotal += produto.getPreco() * qtd;
        }

        double desconto = (subtotal >= 200.0) ? 15.0 : 0.0;
        double totalFinal = subtotal - desconto;

        // Monta entidade Venda
        Venda venda = new Venda();
        venda.setCliente(cliente);
        venda.setDataVenda(LocalDate.now());      // LocalDate (sem hora)
        venda.setDesconto(desconto);
        venda.setValorTotal(totalFinal);

        // Relaciona apenas os produtos (sem quantidade — o modelo atual não tem ItemVenda)
        // Usamos um Set para evitar duplicatas caso o mesmo produto apareça com qtd > 1
        Set<Produto> distintos = new LinkedHashSet<>(produtosCache.values());
        venda.setProdutos(new ArrayList<>(distintos));

        // Salva a venda
        Venda salva = vendaRepository.save(venda);

        // Baixa do estoque + movimentação SAIDA
        for (Map.Entry<Long, Integer> e : itens.entrySet()) {
            Produto produto = produtosCache.get(e.getKey());
            int qtd = e.getValue();

            produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - qtd);
            produtoRepository.save(produto);

            MovimentacaoEstoque mov = new MovimentacaoEstoque();
            mov.setProduto(produto);
            mov.setTipo(TipoMovimento.SAIDA);
            mov.setQuantidade(qtd);
            mov.setDataMovimento(LocalDateTime.now()); // nome correto do setter
            mov.setObservacao("Saída por venda ID " + salva.getId());
            movimentacaoRepository.save(mov);
        }

        return salva;
    }
}
