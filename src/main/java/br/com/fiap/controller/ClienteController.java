package br.com.fiap.controller;

import br.com.fiap.assembler.ClienteAssembler;
import br.com.fiap.entity.Cliente;
import br.com.fiap.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ClienteAssembler clienteAssembler;

    @GetMapping
    public CollectionModel<EntityModel<Cliente>> listarTodos() {
        List<EntityModel<Cliente>> clientes = clienteRepository.findAll()
                .stream()
                .map(clienteAssembler::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(clientes,
                linkTo(methodOn(ClienteController.class).listarTodos()).withSelfRel());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Cliente>> buscarPorId(@PathVariable Long id) {
        return clienteRepository.findById(id)
                .map(clienteAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EntityModel<Cliente>> cadastrar(@RequestBody Cliente cliente) {
        if (cliente.getCpf() != null && clienteRepository.findByCpf(cliente.getCpf()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        Cliente salvo = clienteRepository.save(cliente);
        return ResponseEntity.ok(clienteAssembler.toModel(salvo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Cliente>> atualizar(@PathVariable Long id, @RequestBody Cliente cliente) {
        return clienteRepository.findById(id)
                .map(c -> {
                    c.setNome(cliente.getNome());
                    c.setCpf(cliente.getCpf());
                    c.setTelefone(cliente.getTelefone());
                    c.setEndereco(cliente.getEndereco());
                    Cliente atualizado = clienteRepository.save(c);
                    return ResponseEntity.ok(clienteAssembler.toModel(atualizado));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        return clienteRepository.findById(id)
                .map(c -> {
                    clienteRepository.delete(c);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().<Void>build());
    }
}
