package br.com.fiap.service;

import br.com.fiap.entity.MovimentacaoEstoque;
import br.com.fiap.entity.Produto;
import br.com.fiap.entity.TipoMovimento; 
import br.com.fiap.repository.MovimentacaoEstoqueRepository;
import br.com.fiap.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MovimentacaoEstoqueService {

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    public List<MovimentacaoEstoque> listarTodas() {
        return movimentacaoEstoqueRepository.findAll();
    }

    public List<MovimentacaoEstoque> listarPorProduto(Long produtoId) {
        return movimentacaoEstoqueRepository.findByProdutoId(produtoId);
    }

    /** Entrada manual (reposição). Sem saída manual. */
    public MovimentacaoEstoque registrarEntrada(Long produtoId, int quantidade, String observacao) {
        if (quantidade <= 0) throw new IllegalArgumentException("Quantidade deve ser positiva.");

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        // Atualiza saldo
        produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() + quantidade);
        produtoRepository.save(produto);

        // Registra movimentação
        MovimentacaoEstoque mov = new MovimentacaoEstoque();
        mov.setProduto(produto);
        mov.setTipo(TipoMovimento.ENTRADA);
        mov.setQuantidade(quantidade);
        mov.setDataMovimento(LocalDateTime.now()); // nome correto do setter
        mov.setObservacao(observacao);

        return movimentacaoEstoqueRepository.save(mov);
    }
}
