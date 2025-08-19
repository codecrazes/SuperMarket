package br.com.fiap.controller;

import br.com.fiap.assembler.ProdutoAssembler;
import br.com.fiap.entity.Produto;
import br.com.fiap.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ProdutoAssembler produtoAssembler;

    // 🔹 Listar todos
    @GetMapping
    public CollectionModel<EntityModel<Produto>> listar() {
        List<EntityModel<Produto>> produtos = produtoRepository.findAll()
                .stream()
                .map(produtoAssembler::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(produtos,
                linkTo(methodOn(ProdutoController.class).listar()).withSelfRel());
    }

    // 🔹 Buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Produto>> buscarPorId(@PathVariable Long id) {
        return produtoRepository.findById(id)
                .map(produtoAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 🔹 Criar
    @PostMapping
    public ResponseEntity<EntityModel<Produto>> criar(@RequestBody Produto produto) {
        if (produtoRepository.existsByCodigo(produto.getCodigo())) {
            return ResponseEntity.badRequest().build();
        }
        Produto salvo = produtoRepository.save(produto);
        return ResponseEntity.ok(produtoAssembler.toModel(salvo));
    }

    // 🔹 Atualizar
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Produto>> atualizar(@PathVariable Long id, @RequestBody Produto produtoAtualizado) {
        return produtoRepository.findById(id)
                .map(produto -> {
                    produto.setNome(produtoAtualizado.getNome());
                    produto.setCodigo(produtoAtualizado.getCodigo());
                    produto.setCategoria(produtoAtualizado.getCategoria());
                    produto.setPreco(produtoAtualizado.getPreco());
                    produto.setDataValidade(produtoAtualizado.getDataValidade());
                    Produto atualizado = produtoRepository.save(produto);
                    return ResponseEntity.ok(produtoAssembler.toModel(atualizado));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 🔹 Excluir
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (!produtoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        produtoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
