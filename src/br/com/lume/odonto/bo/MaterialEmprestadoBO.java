package br.com.lume.odonto.bo;

import java.util.List;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Material;
import br.com.lume.odonto.entity.MaterialEmprestado;

public class MaterialEmprestadoBO extends BO<MaterialEmprestado> {

    private Logger log = Logger.getLogger(MaterialEmprestadoBO.class);

    private static final long serialVersionUID = 1L;

    public MaterialEmprestadoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(MaterialEmprestado.class);
    }

    public List<MaterialEmprestado> listMateriaisEmprestado(Material material) throws Exception {

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append("ROW_NUMBER() OVER () * EXTRACT(MICROSECONDS FROM NOW()) * MI.ID AS ID, ");
        sb.append("R.ID_PROFISSIONAL, ");
        sb.append("MI.ID_MATERIAL, ");
        sb.append("R.ID_AGENDAMENTO, ");
        sb.append("MI.QUANTIDADE, ");
        sb.append("R.DATA, ");
        sb.append("'Reserva por Kit' AS TIPO ");
        sb.append("FROM MATERIAL_INDISPONIVEL MI,RESERVA_KIT RK,RESERVA R ");
        sb.append("WHERE MI.STATUS IS NULL ");
        sb.append("AND MI.EXCLUIDO = 'N' ");
        sb.append("AND MI.ID_RESERVA_KIT = RK.ID ");
        sb.append("AND RK.EXCLUIDO = 'N' ");
        sb.append("AND RK.ID_RESERVA = R.ID ");
        sb.append("AND R.EXCLUIDO = 'N' ");
        sb.append("AND RK.STATUS IN ('PE','EN') ");
        sb.append("AND MI.ID_MATERIAL = ?1 ");
        sb.append("UNION ALL ");
        sb.append("SELECT ");
        sb.append("ROW_NUMBER() OVER () * EXTRACT(MICROSECONDS FROM NOW()) * ID AS ID, ");
        sb.append("ID_PROFISSIONAL, ");
        sb.append("ID_MATERIAL, ");
        sb.append("ID_AGENDAMENTO, ");
        sb.append("QUANTIDADE, ");
        sb.append("DATA_ENTREGA AS DATA, ");
        sb.append("'Reserva Manual' AS TIPO ");
        sb.append("FROM ABASTECIMENTO ");
        sb.append("WHERE ID_MATERIAL = ?1 ");
        sb.append("AND STATUS = 'EN' ");
        sb.append("AND EXCLUIDO = 'N' ");
        sb.append("UNION ALL ");
        sb.append("SELECT ");
        sb.append("ROW_NUMBER() OVER () * EXTRACT(MICROSECONDS FROM NOW()) * LK.ID AS ID, ");
        sb.append("R.ID_PROFISSIONAL, ");
        sb.append("MI.ID_MATERIAL, ");
        sb.append("R.ID_AGENDAMENTO, ");
        sb.append("LK.QUANTIDADE, ");
        sb.append("R.DATA, ");
        sb.append("'Lavagem via Empréstimo Kit' AS TIPO ");
        sb.append("FROM LAVAGEM_KIT LK,LAVAGEM L, MATERIAL_INDISPONIVEL MI,RESERVA_KIT RK,RESERVA R ");
        sb.append("WHERE LK.ID_LAVAGEM = L.ID ");
        sb.append("AND LK.ID_MATERIAL_INDISPONIVEL = MI.ID ");
        sb.append("AND MI.ID_MATERIAL = ?1 ");
        sb.append("AND L.STATUS = 'A' ");
        sb.append("AND L.EXCLUIDO = 'N' ");
        sb.append("AND MI.EXCLUIDO = 'N' ");
        sb.append("AND MI.ID_RESERVA_KIT = RK.ID ");
        sb.append("AND RK.ID_RESERVA = R.ID ");
        sb.append("UNION ALL ");
        sb.append("SELECT ");
        sb.append("ROW_NUMBER() OVER () * EXTRACT(MICROSECONDS FROM NOW()) * LK.ID AS ID, ");
        sb.append("A.ID_PROFISSIONAL, ");
        sb.append("A.ID_MATERIAL, ");
        sb.append("A.ID_AGENDAMENTO, ");
        sb.append("LK.QUANTIDADE, ");
        sb.append("A.DATA_ENTREGA AS DATA, ");
        sb.append("'Lavagem via Empréstimo Manual' AS TIPO ");
        sb.append("FROM LAVAGEM_KIT LK,LAVAGEM L,ABASTECIMENTO A ");
        sb.append("WHERE LK.ID_LAVAGEM = L.ID ");
        sb.append("AND LK.ID_ABASTECIMENTO = A.ID ");
        sb.append("AND A.ID_MATERIAL = ?1 ");
        sb.append("AND L.STATUS = 'A' ");
        sb.append("AND L.EXCLUIDO = 'N' ");
        sb.append("UNION ALL ");
        sb.append("SELECT ");
        sb.append("ROW_NUMBER() OVER () * EXTRACT(MICROSECONDS FROM NOW()) * LK.ID AS ID, ");
        sb.append("R.ID_PROFISSIONAL, ");
        sb.append("MI.ID_MATERIAL, ");
        sb.append("R.ID_AGENDAMENTO, ");
        sb.append("LK.QUANTIDADE, ");
        sb.append("R.DATA, ");
        sb.append("'Esterilização via Empréstimo Kit' AS TIPO ");
        sb.append("FROM ESTERILIZACAO_KIT LK,ESTERILIZACAO L, MATERIAL_INDISPONIVEL MI,RESERVA_KIT RK,RESERVA R ");
        sb.append("WHERE LK.ID_ESTERILIZACAO = L.ID ");
        sb.append("AND LK.ID_MATERIAL_INDISPONIVEL = MI.ID ");
        sb.append("AND MI.ID_MATERIAL = ?1 ");
        sb.append("AND L.STATUS IN ('A','E','P') ");
        sb.append("AND L.EXCLUIDO = 'N' ");
        sb.append("AND MI.EXCLUIDO = 'N' ");
        sb.append("AND MI.ID_RESERVA_KIT = RK.ID ");
        sb.append("AND RK.ID_RESERVA = R.ID ");
        sb.append("UNION ALL ");
        sb.append("SELECT ");
        sb.append("ROW_NUMBER() OVER () * EXTRACT(MICROSECONDS FROM NOW()) * LK.ID AS ID, ");
        sb.append("A.ID_PROFISSIONAL, ");
        sb.append("A.ID_MATERIAL, ");
        sb.append("A.ID_AGENDAMENTO, ");
        sb.append("LK.QUANTIDADE, ");
        sb.append("A.DATA_ENTREGA AS DATA, ");
        sb.append("'Esterilização via Empréstimo Manual' AS TIPO ");
        sb.append("FROM ESTERILIZACAO_KIT LK,ESTERILIZACAO L,ABASTECIMENTO A ");
        sb.append("WHERE LK.ID_ESTERILIZACAO = L.ID ");
        sb.append("AND LK.ID_ABASTECIMENTO = A.ID ");
        sb.append("AND A.ID_MATERIAL = ?1 ");
        sb.append("AND L.STATUS IN ('A','E','P') ");
        sb.append("AND L.EXCLUIDO = 'N' ");

        Query query = this.getDao().createNativeQuery(sb.toString(), MaterialEmprestado.class);
        query.setParameter(1, material.getId());
        List<MaterialEmprestado> lista = this.list(query);
        if (lista != null && !lista.isEmpty()) {
            lista.sort((o1, o2) -> o1.getData().compareTo(o2.getData()));
        }
        return lista;

    }
}
