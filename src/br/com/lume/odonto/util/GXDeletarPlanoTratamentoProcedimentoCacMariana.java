package br.com.lume.odonto.util;

import java.math.BigDecimal;
import java.util.Date;

import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;

public class GXDeletarPlanoTratamentoProcedimentoCacMariana {

    public static void main(String[] args) {
        try {
          //  PlanoTratamentoProcedimentoBO bo = new PlanoTratamentoProcedimentoBO();
            long[] ids = new long[] { 25415, 25422, 23367, 26031, 26029, 23253, 23328, 23496 };
            for (long id : ids) {
                PlanoTratamentoProcedimento ptp = PlanoTratamentoProcedimentoSingleton.getInstance().getBo().find(id);
                if (ptp != null) {
                    BigDecimal valorRepasse = PlanoTratamentoProcedimentoSingleton.getInstance().getBo().findValorRepasse(ptp);
                    ptp.setValorRepassado(valorRepasse);
                    ptp.setValorRepasse(valorRepasse);
                    ptp.setStatusPagamento('G');
                    ptp.setDataRepasse(new Date());
                    PlanoTratamentoProcedimentoSingleton.getInstance().getBo().persist(ptp);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //18318,21410,21378,18027,18920,22307,22091,21592,22064,22173,21591,21561,22043,18851,22306,22036,21409,21336,22437,21408,20336,22477,18958,22000,22044,18929,22447,22666,20215,21955,22037,18940,21563,22400,18921,21377,22399,19973,22066,21865,20696,21135,21871,18852,21562,21868,18922,22474,21872,18923,21564,21864,20216,22480,22860,18853,22481,23044,18941,22479,23135,19974,20277,21866,20694,20278,18849,22055,20670,19971,18924,20698,18928,20217,20704,19955,2170420702,18938,19975,21694,20213,20693,21275,18956,22616,21274,20691,18933,22010,20903,20695,22011,18957,21741,22009,18919,20218,18319,18850,22599,22065,19972,21375,22420,18939,21692,22514,20214,21927,22528,19956,21950,18930,21134
}
