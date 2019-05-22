package br.com.lume.odonto.util;

import java.math.BigDecimal;

import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;

public class GXFinalizarNovamenteValorCorreto {

    public static void main(String[] args) {
        try {
           // PlanoTratamentoProcedimentoBO bo = new PlanoTratamentoProcedimentoBO();
            //long[] ids = new long[] { 34744, 34743, 34942, 34254, 34253, 34807, 34789, 33945, 34640, 34528, 34527, 34526, 34525, 34855, 34846, 34713, 34644, 32643, 32629, 32625, 34641, 33389, 34792, 34843, 34827, 34826, 34825, 34821, 34479, 34801, 34618, 34677, 34692, 34691, 34742, 34632, 34741, 33260, 33259, 34690, 27832, 34489, 25786, 34647, 32644, 32645, 32589, 34636, 34635, 34634, 34559, 34561, 34560, 34625, 33392, 34514, 34515, 34522, 33946, 34488, 34487, 34477, 34476, 33251, 33252, 34229, 33425, 34227, 34228, 33258, 34223, 33256, 33924, 33923, 29286, 32786, 32785, 32784, 32692, 32691, 33889, 33072, 33909, 33269, 33063, 33062, 33437, 32590, 32493, 32492, 32723, 32734, 32789, 33416, 33047, 28307, 28306, 33236, 33235, 33399, 33397, 33377, 32635, 32636, 32688, 33376, 33078, 33394, 33393, 29192, 29191, 29190, 32513, 33247, 33246, 33255, 32782, 32787, 32718, 31617, 32627, 32761, 33080, 33055, 32641, 32630, 32631, 32426, 32605, 31142, 31282, 32558, 32563, 32550, 32433, 31609, 32405, 32030, 31227, 31281, 31266, 31058, 31175, 31140, 31154, 31006, 31016, 30938 };
            long[] ids = new long[] { 32641, 33055, 30601, 30587, 32718, 32787, 32782, 26875, 25589, 25563, 23363, 23296, 33246, 33247, 29190, 29191, 29192, 33078, 33376, 33377, 28306, 28307, 33047, 32789, 33437, 33062, 33063, 33269, 24202, 33072, 26028, 23490, 23326, 33889, 26225, 33258, 33425, 33946, 24774, 23411, 23401, 23251, 34522, 34515, 34514, 27018, 34559, 28143, 28144, 30555, 32644, 25744, 26241, 34647, 25786, 27832, 26874, 34632, 34742, 34677, 23388, 34801, 34479, 34821, 34792, 33389, 34641, 32643, 30995, 34644, 34713, 34789, 34807, 34743 };

            //long[] ids = new long[] { 23236 };
            for (long id : ids) {
                PlanoTratamentoProcedimento ptp = PlanoTratamentoProcedimentoSingleton.getInstance().getBo().find(id);
                if (ptp != null) {
                    BigDecimal valorRepasse = PlanoTratamentoProcedimentoSingleton.getInstance().getBo().findValorRepasse(ptp);
                    ptp.setValorRepasse(valorRepasse);
                    if (valorRepasse != null) {
                        System.out.println(id + " " + valorRepasse.toString());
                    } else {
                        System.out.println(id + " null");
                    }
                    PlanoTratamentoProcedimentoSingleton.getInstance().getBo().persist(ptp);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //18318,21410,21378,18027,18920,22307,22091,21592,22064,22173,21591,21561,22043,18851,22306,22036,21409,21336,22437,21408,20336,22477,18958,22000,22044,18929,22447,22666,20215,21955,22037,18940,21563,22400,18921,21377,22399,19973,22066,21865,20696,21135,21871,18852,21562,21868,18922,22474,21872,18923,21564,21864,20216,22480,22860,18853,22481,23044,18941,22479,23135,19974,20277,21866,20694,20278,18849,22055,20670,19971,18924,20698,18928,20217,20704,19955,2170420702,18938,19975,21694,20213,20693,21275,18956,22616,21274,20691,18933,22010,20903,20695,22011,18957,21741,22009,18919,20218,18319,18850,22599,22065,19972,21375,22420,18939,21692,22514,20214,21927,22528,19956,21950,18930,21134
}
