package br.com.fiap.assembler;

import br.com.fiap.controller.ProdutoController;
import br.com.fiap.entity.Produto;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class ProdutoAssembler implements RepresentationModelAssembler<Produto, EntityModel<Produto>> {

    @Override
    public EntityModel<Produto> toModel(Produto produto) {
        return EntityModel.of(produto,
                linkTo(methodOn(ProdutoController.class).buscarPorId(produto.getId())).withSelfRel(),
                linkTo(methodOn(ProdutoController.class).listar()).withRel("produtos") 
        );
    }
}
