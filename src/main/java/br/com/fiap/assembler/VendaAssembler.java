package br.com.fiap.assembler;

import br.com.fiap.controller.VendaController;
import br.com.fiap.entity.Venda;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class VendaAssembler implements RepresentationModelAssembler<Venda, EntityModel<Venda>> {

    @Override
    public EntityModel<Venda> toModel(Venda venda) {
        return EntityModel.of(venda,
                linkTo(methodOn(VendaController.class).buscarPorId(venda.getId())).withSelfRel(),
                linkTo(methodOn(VendaController.class).listarTodas()).withRel("vendas") 
        );
    }
}
