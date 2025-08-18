package br.com.fiap.controller;

import br.com.fiap.entity.MovimentacaoEstoque;
import br.com.fiap.entity.Produto;
import br.com.fiap.entity.TipoMovimento;
import br.com.fiap.repository.MovimentacaoEstoqueRepository;
import br.com.fiap.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/movimentacoes-estoque")
public class MovimentacaoEstoqueController {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    // Lista todas as movimentações (histórico)
    @GetMapping
    public List<MovimentacaoEstoque> listarTodas() {
        return movimentacaoRepository.findAll();
    }

    // Consulta movimentações por produto
    @GetMapping("/produto/{produtoId}")
    public List<MovimentacaoEstoque> listarPorProduto(@PathVariable Long produtoId) {
        return movimentacaoRepository.findByProdutoId(produtoId);
    }

    // Registrar entrada manual (reposição de estoque)
    @PostMapping("/entrada/{produtoId}")
    public ResponseEntity<EntityModel<MovimentacaoEstoque>> registrarEntrada(
            @PathVariable Long produtoId,
            @RequestParam Integer quantidade,
            @RequestParam(required = false) String observacao) {

        return produtoRepository.findById(produtoId)
                .map(produto -> {
                    // Atualiza o estoque do produto
                    produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() + quantidade);
                    produtoRepository.save(produto);

                    // Registra movimentação
                    MovimentacaoEstoque mov = new MovimentacaoEstoque();
                    mov.setProduto(produto);
                    mov.setTipo(TipoMovimento.ENTRADA);
                    mov.setQuantidade(quantidade);
                    mov.setDataMovimento(LocalDateTime.now());
                    mov.setObservacao(observacao);

                    movimentacaoRepository.save(mov);

                    // HATEOAS links
                    EntityModel<MovimentacaoEstoque> model = EntityModel.of(mov);
                    model.add(linkTo(methodOn(MovimentacaoEstoqueController.class).listarTodas()).withRel("todas-movimentacoes"));
                    model.add(linkTo(methodOn(MovimentacaoEstoqueController.class).listarPorProduto(produtoId)).withRel("movimentacoes-do-produto"));

                    return ResponseEntity.ok(model);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}

