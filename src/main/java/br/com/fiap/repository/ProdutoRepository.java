package br.com.fiap.repository;

import br.com.fiap.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    Optional<Produto> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}
