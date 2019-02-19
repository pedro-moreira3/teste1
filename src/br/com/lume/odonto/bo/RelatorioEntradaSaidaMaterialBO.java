package br.com.lume.odonto.bo;

import java.util.List;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Item;
import br.com.lume.odonto.entity.Local;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.RelatorioEntradaSaidaMaterial;

public class RelatorioEntradaSaidaMaterialBO extends BO<RelatorioEntradaSaidaMaterial> {

    private Logger log = Logger.getLogger(RelatorioEntradaSaidaMaterialBO.class);

    private static final long serialVersionUID = 1L;

    public RelatorioEntradaSaidaMaterialBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(RelatorioEntradaSaidaMaterial.class);
    }

    public List<RelatorioEntradaSaidaMaterial> listAllByFilterToReport(Local local, Item item, Profissional profissional) {
        try {
            String sql = "select " + "* " + "from " + "( " + "select " + "m.ID || '-' || m.DATA_CADASTRO as ID, " + "i.DESCRICAO AS ITEM , " + "i.UNIDADE_MEDIDA as UNIDADE_MEDIDA, " + "i.ESTOQUE_MINIMO as ESTOQUE_MINIMO, " + "m.ID_EMPRESA, " + "m.ID_LOCAL as LOCAL_MATERIAL, " + "m.QUANTIDADE_TOTAL , " + "(m.QUANTIDADE*m.TAMANHO_UNIDADE*m.valor)/(m.QUANTIDADE*m.TAMANHO_UNIDADE) AS VALOR_MEDIO, " + "m.QUANTIDADE*m.TAMANHO_UNIDADE*m.valor as VALOR_TOTAL, " + "M.STATUS, " + "M.EXCLUIDO, " + "null ENTREGA, " + "m.DATA_CADASTRO ENTRADA, " + "null procedimento, " + "m.TAMANHO_UNIDADE, " + "m.QUANTIDADE_TOTAL QUANTIDADE, " + "m.VALIDADE, " + "null AS ID_PROFISSIONAL, " + "null AS LOCAL_RESERVA, " + "0 AS ESTOQUE, " + "m.DATA_CADASTRO as ORDENACAO " + "from Material as m, Item as i " + "where i.ID = m.ID_ITEM and m.quantidade >0" + " UNION " + "select " + "m.ID ||  '-' || rk.DATA_ENTREGA as ID, " + "i.DESCRICAO AS ITEM , " + "i.UNIDADE_MEDIDA as UNIDADE_MEDIDA, " + "i.ESTOQUE_MINIMO as ESTOQUE_MINIMO, " + "m.ID_EMPRESA, " + "null as LOCAL_MATERIAL, " + "m.QUANTIDADE_TOTAL, " + "(m.QUANTIDADE*m.TAMANHO_UNIDADE*m.valor)/(m.QUANTIDADE*m.TAMANHO_UNIDADE) AS VALOR_MEDIO, " + "(m.QUANTIDADE*m.TAMANHO_UNIDADE*m.valor) AS VALOR_TOTAL, " + "M.STATUS, " + "M.EXCLUIDO, " + "rk.DATA_ENTREGA ENTREGA, " + "null ENTRADA, " + "rk.ID_PROCEDIMENTO procedimento, " + "m.TAMANHO_UNIDADE, " + "mi.QUANTIDADE , " + "m.VALIDADE, " + "r.ID_PROFISSIONAL , " + "r.ID_LOCAL AS LOCAL_RESERVA, " + "1 AS ESTOQUE," + "rk.DATA_ENTREGA as ORDENACAO " + "from material_indisponivel mi, reserva_kit rk, reserva r, material m, item i " + "where rk.id = mi.ID_RESERVA_KIT " + "and rk.ID_RESERVA = r.id " + "and m.id = mi.ID_MATERIAL " + "and i.id = m.id_item " + "and rk.STATUS ='EN' and m.quantidade >0" + " UNION " + "select " + "m.ID || '-' || a.DATA_ENTREGA as ID, " + "i.DESCRICAO AS ITEM , " + "i.UNIDADE_MEDIDA as UNIDADE_MEDIDA, " + "i.ESTOQUE_MINIMO as ESTOQUE_MINIMO, " + "m.ID_EMPRESA, " + "null as LOCAL_MATERIAL, " + "m.QUANTIDADE_TOTAL, " + "(m.QUANTIDADE*m.TAMANHO_UNIDADE*m.valor)/(m.QUANTIDADE*m.TAMANHO_UNIDADE) AS VALOR_MEDIO, " + "(m.QUANTIDADE*m.TAMANHO_UNIDADE*m.valor) AS VALOR_TOTAL, " + "M.STATUS, " + "M.EXCLUIDO, " + "a.DATA_ENTREGA ENTREGA, " + "null ENTRADA, " + "null procedimento, " + "m.TAMANHO_UNIDADE, " + "a.QUANTIDADE , " + "m.VALIDADE, " + "a.ID_PROFISSIONAL , " + "null AS LOCAL_RESERVA, " + "1 AS ESTOQUE, " + "a.DATA_ENTREGA as ORDENACAO " + "from  abastecimento a, material m, item i " + "where a.ID_MATERIAL = m.id " + "and i.id = m.id_item and m.quantidade >0" + ") " + "where ID_EMPRESA = " + ProfissionalBO.getProfissionalLogado().getIdEmpresa();
            if (local != null) {
                sql += " and (LOCAL_MATERIAL = " + local.getId() + " or LOCAL_RESERVA = " + local.getId() + " )";
            }
            if (item != null) {
                sql += " and ITEM= '" + item.getDescricao() + "'";
            }
            if (profissional != null) {
                sql += " and id_profissional = " + profissional.getId();
            }
            sql += " and STATUS = 'A' ";
            sql += " and EXCLUIDO = 'N'";
            sql += " order by ORDENACAO ";
            Query query = this.getDao().createNativeQuery(sql, RelatorioEntradaSaidaMaterial.class);
            return this.list(query);
        } catch (Exception e) {
            this.log.error("Erro no listAllByFilterToReport", e);
        }
        return null;
    }

}
