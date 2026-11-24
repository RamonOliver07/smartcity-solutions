package com.example.backend.repository;

import com.example.backend.model.Conexao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ConexaoRepository extends JpaRepository<Conexao, Long> {

    @Transactional
    void deleteByOrigemIdOrDestinoId(int origemId, int destinoId);

    @Transactional
    void deleteByOrigemIdAndDestinoId(int origemId, int destinoId);
}