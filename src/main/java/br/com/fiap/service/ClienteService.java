package br.com.fiap.service;

import br.com.fiap.entity.Cliente;
import br.com.fiap.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public Optional<Cliente> buscarPorId(Long id) {
        return clienteRepository.findById(id);
    }

    public Cliente cadastrar(Cliente cliente) {
        if (clienteRepository.existsByCpf(cliente.getCpf())) {
            throw new RuntimeException("Cliente com este CPF já existe!");
        }
        return clienteRepository.save(cliente);
    }

    public Cliente atualizar(Long id, Cliente cliente) {
        return clienteRepository.findById(id)
                .map(c -> {
                    c.setNome(cliente.getNome());
                    c.setCpf(cliente.getCpf());
                    c.setTelefone(cliente.getTelefone());
                    c.setEndereco(cliente.getEndereco());
                    return clienteRepository.save(c);
                })
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
    }

    public void excluir(Long id) {
        clienteRepository.deleteById(id);
    }
}
