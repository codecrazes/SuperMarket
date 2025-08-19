package br.com.fiap.service;

import br.com.fiap.entity.Produto;
import br.com.fiap.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    public Optional<Produto> buscarPorId(Long id) {
        return produtoRepository.findById(id);
    }

    public Produto cadastrar(Produto produto) {
        if (produtoRepository.existsByCodigo(produto.getCodigo())) {
            throw new RuntimeException("Produto com este código já existe!");
        }
        return produtoRepository.save(produto);
    }

    public Produto atualizar(Long id, Produto produto) {
        return produtoRepository.findById(id)
                .map(p -> {
                    p.setNome(produto.getNome());
                    p.setCodigo(produto.getCodigo());
                    p.setCategoria(produto.getCategoria());
                    p.setPreco(produto.getPreco());
                    p.setDataValidade(produto.getDataValidade());
                    return produtoRepository.save(p);
                })
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
    }

    public void excluir(Long id) {
        produtoRepository.deleteById(id);
    }
}
