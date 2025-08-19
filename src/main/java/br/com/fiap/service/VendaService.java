package br.com.fiap.service;

import br.com.fiap.entity.Cliente;
import br.com.fiap.entity.Venda;
import br.com.fiap.repository.ClienteRepository;
import br.com.fiap.repository.VendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class VendaService {

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    public List<Venda> listarTodas() {
        return vendaRepository.findAll();
    }

    /**
     * Registra uma venda.
     * @param clienteId id do cliente
     * @param valorTotal valor total da venda
     */
    @Transactional
    public Venda registrarVenda(Long clienteId, Double valorTotal) {
        if (valorTotal == null || valorTotal <= 0) {
            throw new IllegalArgumentException("O valor da venda deve ser maior que zero.");
        }

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        double desconto = (valorTotal >= 200.0) ? 15.0 : 0.0;
        double totalFinal = valorTotal - desconto;

        Venda venda = new Venda();
        venda.setCliente(cliente);
        venda.setDataVenda(LocalDate.now());
        venda.setDesconto(desconto);
        venda.setValorTotal(totalFinal);

        return vendaRepository.save(venda);
    }
}
