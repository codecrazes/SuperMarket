package br.com.fiap.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "TB_MOV_ESTOQUE")
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mov_estoque_seq")
    @SequenceGenerator(name = "mov_estoque_seq", sequenceName = "SEQ_MOV_ESTOQUE", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimento tipo;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "data_movimento", nullable = false)
    private LocalDateTime dataMovimento;

    @Column(length = 500)
    private String observacao;
}