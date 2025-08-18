package br.com.fiap.assembler;

import br.com.fiap.controller.ClienteController;
import br.com.fiap.entity.Cliente;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class ClienteAssembler implements RepresentationModelAssembler<Cliente, EntityModel<Cliente>> {

    @Override
    public EntityModel<Cliente> toModel(Cliente cliente) {
        return EntityModel.of(cliente,
                linkTo(methodOn(ClienteController.class).buscarPorId(cliente.getId())).withSelfRel(),
                linkTo(methodOn(ClienteController.class).listarTodos()).withRel("clientes")
        );
    }
}
