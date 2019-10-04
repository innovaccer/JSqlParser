/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CCJSqlParserUtilTest {

    public CCJSqlParserUtilTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of parseExpression method, of class CCJSqlParserUtil.
     */
    @Test
    public void testParseExpression() throws Exception {
        Expression result = CCJSqlParserUtil.parseExpression("a+b");
        assertEquals("a + b", result.toString());
        assertTrue(result instanceof Addition);
        Addition add = (Addition) result;
        assertTrue(add.getLeftExpression() instanceof Column);
        assertTrue(add.getRightExpression() instanceof Column);
    }

    @Test
    public void testParseExpression2() throws Exception {
        Expression result = CCJSqlParserUtil.parseExpression("2*(a+6.0)");
        assertEquals("2 * (a + 6.0)", result.toString());
        assertTrue(result instanceof Multiplication);
        Multiplication mult = (Multiplication) result;
        assertTrue(mult.getLeftExpression() instanceof LongValue);
        assertTrue(mult.getRightExpression() instanceof Parenthesis);
    }

    @Test(expected = JSQLParserException.class)
    public void testParseExpressionNonPartial() throws Exception {
        CCJSqlParserUtil.parseExpression("a+", false);

    }

    @Test(expected = JSQLParserException.class)
    public void testParseExpressionFromStringFail() throws Exception {
         CCJSqlParserUtil.parse("whatever$");
    }

    @Test(expected = JSQLParserException.class)
    public void testParseExpressionFromRaderFail() throws Exception {
         CCJSqlParserUtil.parse(new StringReader("whatever$"));
    }

    @Test
    public void testParseExpressionNonPartial2() throws Exception {
        Expression result = CCJSqlParserUtil.parseExpression("a+", true);
        assertEquals("a", result.toString());
    }

    @Test
    public void testParseCondExpression() throws Exception {
        Expression result = CCJSqlParserUtil.parseCondExpression("a+b>5 and c<3");
        assertEquals("a + b > 5 AND c < 3", result.toString());
    }

    @Test(expected = JSQLParserException.class)
    public void testParseCondExpressionFail() throws Exception {
       CCJSqlParserUtil.parseCondExpression(";");

    }

    @Test(expected = JSQLParserException.class)
    public void testParseFromStreamFail() throws Exception {
       CCJSqlParserUtil.parse(new ByteArrayInputStream("BLA".getBytes(StandardCharsets.UTF_8)));

    }

    @Test(expected = JSQLParserException.class)
    public void testParseFromStreamWithEncodingFail() throws Exception {
       CCJSqlParserUtil.parse(new ByteArrayInputStream("BLA".getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8.name());

    }

    @Test
    public void testParseCondExpressionNonPartial() throws Exception {
        Expression result = CCJSqlParserUtil.parseCondExpression("x=92 and y=29", false);
        assertEquals("x = 92 AND y = 29", result.toString());
    }

    @Test(expected = JSQLParserException.class)
    public void testParseCondExpressionNonPartial2() throws Exception {
        Expression result = CCJSqlParserUtil.parseCondExpression("x=92 lasd y=29", false);
        System.out.println(result.toString());
    }

    @Test
    public void testParseCondExpressionPartial2() throws Exception {
        Expression result = CCJSqlParserUtil.parseCondExpression("x=92 lasd y=29", true);
        assertEquals("x = 92", result.toString());
    }

    @Test
    public void testParseCondExpressionIssue471() throws Exception {
        Expression result = CCJSqlParserUtil.parseCondExpression("(SSN,SSM) IN ('11111111111111', '22222222222222')");
        assertEquals("(SSN, SSM) IN ('11111111111111', '22222222222222')", result.toString());
    }

    @Test
    public void testParseStatementsIssue691() throws Exception {
        Statements result = CCJSqlParserUtil.parseStatements(
                "select * from dual;\n"
                + "\n"
                + "select\n"
                + "*\n"
                + "from\n"
                + "dual;\n"
                + "\n"
                + "select *\n"
                + "from dual;");
        assertEquals("SELECT * FROM dual;\n"
                + "SELECT * FROM dual;\n"
                + "SELECT * FROM dual;\n", result.toString());
    }

    @Test(expected = JSQLParserException.class)
    public void testParseStatementsFail() throws Exception {
       CCJSqlParserUtil.parseStatements("select * from dual;WHATEVER!!");
    }

    @Test(expected = JSQLParserException.class)
    public void testParseASTFail() throws Exception {
       CCJSqlParserUtil.parseAST("select * from dual;WHATEVER!!");
    }

    @Test
    public void testParseStatementsIssue691_2() throws Exception {
        Statements result = CCJSqlParserUtil.parseStatements(
                "select * from dual;\n"
                + "---test");
        assertEquals("SELECT * FROM dual;\n", result.toString());
    }

    @Test
    public void testParseStatementIssue742() throws Exception {
        Statements result = CCJSqlParserUtil.parseStatements("CREATE TABLE `table_name` (\n" +
                "  `id` bigint(20) NOT NULL AUTO_INCREMENT,\n" +
                "  `another_column_id` bigint(20) NOT NULL COMMENT 'column id as sent by SYSTEM',\n" +
                "  PRIMARY KEY (`id`),\n" +
                "  UNIQUE KEY `uk_another_column_id` (`another_column_id`)\n" +
                ")");
        assertEquals("CREATE TABLE `table_name` (`id` bigint (20) NOT NULL AUTO_INCREMENT, `another_column_id` " +
                "bigint (20) NOT NULL COMMENT 'column id as sent by SYSTEM', PRIMARY KEY (`id`), UNIQUE KEY `uk_another_column_id` " +
                "(`another_column_id`));\n", result.toString());
    }

    @Test
    public void testParseStatementsIndata() throws Exception {


        Insert insert = (Insert)CCJSqlParserUtil.parse("insert into l2.pd_activity(empi, id, cid , clid, clsfdt, clsldt, efdt, eldt, pfdt, pc, pcs, pm1, pm2, pm3, pm4, pm5, uqt, rcc, catp, cloutfl, st, ingdt, vdml, vdnm, acoid, acon, sfn,clpic, rcdt, pipid, incid, incn, inpid, inpn, drgn, drg, pd, pdcs, csbdt, cpdt, cadt, capdt, at, poac, cltc, cftc, atc, stpc, bt, slc , orgtin, orgn, alat, pih, coph, ddah, dea, adjs, adjsn, rsc, cmnpr, cnppc, drdc, ftnpi, spnpi, atpnpi, otpnpi, opnpi, spn, atpn, otpn, ftn, tt, ftccn, fn, mn, ln, dob, dod, gn, ethn, racen, ms, pl, bfsdcd, hpstdt, hpenddt, orec, byind, pthcno, df,bedpa,bedpb, idt, pz, pst, sstp, inptp, ddt, pcpid, tid, tbid, pdn, eid, ptt, rxn, prdt, rxc, rxfdt, pid, pn, pftm, spid, inpfdt, inpldt, oc, pon, ofdt, rn, rc, rdt, cltp,spsc,tcamt,caia,imeamt,cdpa,cuca,cdsa,ahc,sno)\n" +
                "(\n" +
                "Select distinct \n" +
                "clmline.empi AS empi,\n" +
                "clmline.local_member_id as id,\n" +
                "clmline.claim_id AS cid ,\n" +
                "clmline.claim_line_id AS clid,\n" +
                "clmline.first_date_of_service AS clsfdt,\n" +
                "clmline.last_date_of_service  AS clsldt,\n" +
                "clmhead.first_date_of_service AS efdt,\n" +
                "clmhead.last_date_of_service  AS eldt,\n" +
                "Case when clmline.source_file_name like '%CCLF5%' THEN cast(Null as date) \n" +
                "when clmline.source_file_name like '%CCLF6%' then cast(Null as date)\n" +
                "else clmhead.first_date_of_service end as pfdt,\n" +
                "nvl(clmline.procedure_code,'') AS pc,\n" +
                "nvl(clmline.procedure_coding_system,'') AS pcs,\n" +
                "nvl(clmline.modifier_code_1,'') AS pm1,\n" +
                "nvl(clmline.modifier_code_2,'') AS pm2,\n" +
                "nvl(clmline.modifier_code_3,'') AS pm3,\n" +
                "nvl(clmline.modifier_code_4,'') AS pm4,\n" +
                "nvl(clmline.modifier_code_5,'') AS pm5,\n" +
                "case when cast(clmline.service_unit_quantity as varchar(50)) is null then ''  else \n" +
                "cast(clmline.service_unit_quantity as varchar(50)) end AS uqt,\n" +
                "case when clmline.revenue_center_code is null then '' else clmline.revenue_center_code end AS rcc,\n" +
                "case when cast(clmline.line_insurance_paid_amount as varchar(50)) is null then ''  else cast(clmline.line_insurance_paid_amount as varchar(50)) end AS catp,\n" +
                "nvl(clmline.out_network_flag,'') AS cloutfl,\n" +
                "cast(clmline.source_type as varchar(50)) as st,\n" +
                "getdate() AS ingdt,\n" +
                "cast(clmline.emr_version as varchar(50)) AS vdml,\n" +
                "cast(clmline.emr_name as varchar(50)) AS vdnm,\n" +
                "cast(clmline.aco_id as varchar(50)) AS acoid,\n" +
                "cast(clmline.aco_name as varchar(50)) AS acon,\n" +
                "CASE WHEN clmline.source_file_name LIKE '%CCLF2%' THEN 'CCLF2'\n" +
                "WHEN clmline.source_file_name LIKE '%CCLF5%' THEN 'CCLF5'\n" +
                "WHEN clmline.source_file_name LIKE '%CCLF6%' THEN 'CCLF6'\n" +
                "ELSE clmline.source_file_name END  as sfn,\n" +
                "clmline.service_reason as clpic,\n" +
                "case when clmline.source_type = 'Claims' and clmline.aco_id = '3486' \n" +
                "and clmline.source_file_name like '%CCLF%' then\n" +
                "cast((SUBSTRING(clmline.source_file_name,20,4)||'01') as date) end as rcdt,\n" +
                "cast(clmline.pipeline_id as varchar(200)) as pipid,\t\t----datatype change 1\n" +
                "clmhead.payer_id AS incid,\n" +
                "clmhead.payer_name AS incn,\n" +
                "clmhead.plan_id AS inpid,\n" +
                "clmhead.plan_name As inpn,\n" +
                "nvl(clmhead.drg,'') AS drgn,\n" +
                "decode(clmhead.drg_code,null,'','0000','',clmhead.drg_code) AS drg,\n" +
                "nvl(clmhead.primary_diagnosis_code,'') AS pd,\n" +
                "case when clmhead.diagnosis_coding_system = 'I9' THEN 'ICD-9'\n" +
                "when clmhead.diagnosis_coding_system = 'I10' THEN 'ICD-10'\n" +
                "when clmhead.diagnosis_coding_system = 'U' THEN 'U'\n" +
                "ELSE 'U' END as pdcs,\n" +
                "clmhead.claim_subsmission_date AS csbdt,\n" +
                "clmhead.claim_processing_date AS cpdt,\n" +
                "clmhead.claim_adjudication_date AS cadt,\n" +
                "clmhead.claim_approval_date AS capdt,\n" +
                "'' AS at,\n" +
                "nvl(clmhead.present_at_admission,'') AS poac,\n" +
                "CASE WHEN clmhead.claim_type = 'I' THEN 'Institutional'\n" +
                "WHEN clmhead.claim_type = 'P' THEN 'Professional'\n" +
                "WHEN clmhead.claim_type = 'DME' THEN 'DME'\n" +
                "else '' END AS cltc,\n" +
                "nvl(clmhead.facility_type,'') AS cftc,\n" +
                "nvl(clmhead.admission_type,'') AS atc,\n" +
                "nvl(clmline.service_type,'') AS stpc,\n" +
                "nvl(clmhead.type_of_bill,'') AS bt,\n" +
                "nvl(clmhead.place_of_service,'') AS slc ,\n" +
                "nvl(clmhead.tax_id,'') AS orgtin,\n" +
                "nvl(clmhead.servicing_tax_org_name,'') AS orgn,\n" +
                "case when cast(clmline.line_claim_amount as varchar(50)) is null then ''  \n" +
                "else cast(clmline.line_claim_amount as varchar(50)) end AS alat,\n" +
                "case when cast(clmhead.total_insurance_paid_amount as varchar(50)) is null then '' \n" +
                "else cast(clmhead.total_insurance_paid_amount as varchar(50)) end AS pih,\n" +
                "case when cast(clmhead.co_pay as varchar(50)) is null then '' \n" +
                "else cast(clmhead.co_pay as varchar(50)) end AS coph,\n" +
                "case when cast(clmhead.deductible as varchar(50)) is null then '' \n" +
                "else cast(clmhead.deductible as varchar(50))end AS ddah,\n" +
                "case when cast(clmhead.eligible_amount as varchar(50)) is null then '' else cast(clmhead.eligible_amount as varchar(300)) end AS dea,\n" +
                "nvl(clmhead.adjustment_status,'') AS adjs,\n" +
                "nvl(clmhead.adjustment_reason,'') AS adjsn,\n" +
                "nvl(clmhead.referral_source_code,'') AS rsc,\n" +
                "nvl(clmhead.denial_reason,'') AS cmnpr,\n" +
                "nvl(clmhead.claim_nch_code,'') as cnppc,\n" +
                "nvl(clmhead.discharge_disposition_code,'') as drdc,\n" +
                "nvl(clmhead.practice_npi,'') AS ftnpi,\n" +
                "case when clmline.source_file_name like '%CCLF5%' THEN nvl(clmline.servicing_provider_npi,'')\n" +
                "when clmline.source_file_name like '%CCLF6%' THEN '' \n" +
                "else nvl(clmhead.servicing_provider_npi,'') END AS spnpi,\n" +
                "nvl(clmhead.attending_provider_npi,'') AS atpnpi,\n" +
                "nvl(clmhead.other_provider_npi,'') AS otpnpi,\n" +
                "case when clmhead.source_file_name like '%CCLF6%' THEN nvl(clmhead.servicing_provider_npi,'') ELSE '' end AS opnpi,\n" +
                "case when clmline.source_file_name like '%CCLF5%' THEN nvl(clmline.servicing_provider,'')\n" +
                "when clmline.source_file_name like '%CCLF6%' THEN ''\n" +
                "else nvl(clmhead.servicing_provider,'') end AS spn,\n" +
                "nvl(clmhead.attending_provider,'') AS atpn,\n" +
                "nvl(clmhead.other_provider,'') AS otpn,\n" +
                "nvl(clmhead.practice_name,'') AS ftn,\n" +
                "case when clmhead.claim_query_code ='0' then 'Credit adjustment'\n" +
                "when clmhead.claim_query_code ='1' then 'Interim bill'\n" +
                "when clmhead.claim_query_code ='2' then 'HHA benefits exhausted'\n" +
                "when clmhead.claim_query_code ='3' then 'Final bill'\n" +
                "when clmhead.claim_query_code ='4' then 'Discharge notice'\n" +
                "when clmhead.claim_query_code ='5' then 'Debit adjustment' else '' end as tt,\n" +
                "nvl(clmhead.ccn,'') AS ftccn,\n" +
                "nvl(mem.first_name,'') AS fn,\n" +
                "nvl(mem.middle_name,'') AS mn,\n" +
                "nvl(mem.last_name,'') AS ln,\n" +
                "mem.birth_date AS dob,\n" +
                "mem.deceased_date AS dod,\n" +
                "nvl(mem.gender,'') AS gn,\n" +
                "nvl(mem.ethnicity,'') as ethn,\n" +
                "nvl(mem.race,'') AS racen,\n" +
                "nvl(mem.marital_status,'') AS ms,\n" +
                "nvl(mem.primary_language,'') AS pl,\n" +
                "nvl(mem.dual_status_code,'') AS bfsdcd,\n" +
                "mem.hospice_enrollment_date AS hpstdt,\n" +
                "mem.hospice_termination_date AS hpenddt,\n" +
                "nvl(mem.reason_entitlement,'')as orec,\n" +
                "nvl(mem.buy_in,'')as byind,\n" +
                "nvl(alt.alternate_patient_id,'') as pthcno,\n" +
                "case when mem.deceased_date is null then 'N' else 'Y' END AS df,\n" +
                "mem.beneficiary_enrollment_date_part_a as bedpa,\n" +
                "mem.beneficiary_enrollment_date_part_b as bedpb,\n" +
                "'HICNO' as idt,\t\t\t\t-- CHANGE\n" +
                "'' as pz,\n" +
                "'' as pst,\n" +
                "'MSSP TRACK 1' AS sstp,\n" +
                "'' AS inptp,\n" +
                "CAST(NULL AS DATE) AS ddt,\n" +
                "'' AS pcpid,\n" +
                "'' AS tid,\n" +
                "'' AS tbid,\n" +
                "'' AS pdn,\n" +
                "'' AS eid,\n" +
                "'' AS ptt,\n" +
                "'' AS rxn,\n" +
                "CAST(NULL AS DATE) AS prdt,\n" +
                "'' AS rxc,\n" +
                "CAST(NULL AS DATE) AS rxfdt,\n" +
                "'' AS pid,\n" +
                "'' AS pn,\n" +
                "'' AS pftm,\n" +
                "'' AS spid,\n" +
                "CAST(NULL AS DATE) AS inpfdt,\n" +
                "CAST(NULL AS DATE) AS inpldt,\n" +
                "'' AS oc,\n" +
                "'' AS pon,\n" +
                "CAST(NULL AS DATE) AS ofdt,\n" +
                "'' AS rn,\n" +
                "'' AS rc,\n" +
                "CAST(NULL AS DATE) AS rdt,\n" +
                "'claimline' as cltp,\n" +
                "case when clmline.source_file_name like '%CCLF5%'and clmline.source_file_name = clmhead.source_file_name\n" +
                "THEN clmline.provider_speciality ELSE '' END as spsc,\n" +
                "clmhead.total_charge_amount as tcamt,\n" +
                "clmhead.claim_applicable_ime_amount as caia,\n" +
                "clmhead.claim_ime_amount as imeamt,\n" +
                "clmhead.claim_disproportionate_amount as cdpa,\n" +
                "clmhead.claim_uncompensated_care_amount as cuca,\n" +
                "clmhead.Claim_disproportionate_share_amount as cdsa,\n" +
                "\n" +
                "\n" +
                "/*case when cast(clmhead.total_charge_amount as varchar(300)) is null then '' \n" +
                "else cast(clmhead.total_claim_amount as varchar(300)) end AS tcamt,\n" +
                "case when cast(clmhead.claim_applicable_ime_amount as varchar(300)) is null then '' \n" +
                "else cast(clmhead.claim_applicable_ime_amount as varchar(300)) end AS caia,\n" +
                "case when cast(clmhead.claim_ime_amount as varchar(300)) is null then '' \n" +
                "else cast(clmhead.claim_ime_amount as varchar(300)) end AS imeamt,\n" +
                "case when cast(clmhead.claim_disproportionate_amount as varchar(300)) is null then '' \n" +
                "else cast(clmhead.claim_disproportionate_amount as varchar(300)) end AS cdpa,\n" +
                "case when cast(clmhead.claim_uncompensated_care_amount as varchar(300)) is null then '' \n" +
                "else cast(clmhead.claim_uncompensated_care_amount as varchar(300)) end AS cuca,\n" +
                "case when cast(clmhead.Claim_disproportionate_share_amount as varchar(300)) is null then '' \n" +
                "else cast(clmhead.Claim_disproportionate_share_amount as varchar(300)) end AS cdsa,*/\n" +
                "\n" +
                "nvl(cast(clmline.apc_hipps_code as varchar(30)),'') as ahc,\n" +
                "/*cast(npi1.ptxcd as varchar(50)) as spsc,\t\n" +
                "cast(npi1.psn as varchar(100)) as sps,\n" +
                "*/\n" +
                "('Claims'||':'||cast(sstp as varchar(50))||':'||cast(clmline.local_member_id as varchar(20))||':'||cast(eid as varchar(20))||':'||isnull((cast(efdt as varchar(50))),\n" +
                "'')||':'||isnull((cast(ddt as varchar(50))),\n" +
                "'')||':'||cast(pd as varchar(50))||':'||isnull((cast(prdt as varchar(50))),\n" +
                "'')||':'||cast(rxc as varchar(50))||':'||isnull((cast(rxfdt as varchar(50))),\n" +
                "'')||':'||cast(oc as varchar(50))||':'||cast(pon as varchar(50))||':'||isnull((cast(ofdt as varchar(50))),\n" +
                "'')||':'||cast(pid as varchar(20))||':'||cast(pc as varchar(20))||':'||isnull((cast(pfdt as varchar(50))),\n" +
                "'')||':'||cast(pftm as varchar(20))||':'||cast(rc as varchar(20))||':'||isnull((cast(rdt as varchar(50))),\n" +
                "'')||':'||cast(inpid as varchar(20))||':'||cast(incid as varchar(20))||':'||cast(tid as varchar(20))||':'||cast(tbid as varchar(50))||':'||cast(cid as varchar(30))||':'||cast(clid as varchar(30))||':'||isnull((cast(eldt as varchar(50))),\n" +
                "'')||':'||cast(acoid as varchar(20))||':'||cast(pn as varchar(50))||':'||cast(pdn as varchar(50))||':'||cast(rxn as varchar(50))||':'||cast(rn as varchar(50))||':'||cast(pcpid as varchar(50))||':'||cast(spid as varchar(50))||':'||nvl(spnpi,'')||':'||cast(ptt as varchar(50))||':'||isnull((cast(inpfdt as varchar(50))),\n" +
                "'')||':'||isnull((cast(inpldt as varchar(50))),\n" +
                "'')||':'||cast(id as varchar(50))||':'||cast(cltp as varchar(50))) AS sno \n" +
                "\n" +
                "from l2.claim_line clmline\n" +
                "left join (select distinct clmhead.*,\n" +
                "case when source_type = 'Claims' and aco_id = '3486' and source_file_name like '%CCLF%'\n" +
                "then cast((SUBSTRING(source_file_name,20,4)||'01') as date) end as dt\n" +
                "from l2.claim_header clmhead\n" +
                "where source_type = 'Claims' and aco_id = '3486' \n" +
                "and source_file_name like '%CCLF%' and dt = '2017-12-01')clmhead \n" +
                "\n" +
                "ON clmline.claim_id = clmhead.claim_id\n" +
                "and (case when clmline.aco_id = '3486' and clmline.source_file_name like '%CCLF%' then \n" +
                "cast((SUBSTRING(clmline.source_file_name,20,4)||'01') as date) else null end )=clmhead.dt\n" +
                "and clmline.aco_id = clmhead.aco_id\n" +
                "\n" +
                "left join l2.member mem\n" +
                "--ON clmline.empi=mem.empi\n" +
                "on clmline.local_member_id=mem.local_member_id\n" +
                "and clmline.birth_date=mem.birth_date\n" +
                "and clmline.source_type=mem.source_type\n" +
                "and clmline.aco_id = mem.aco_id\n" +
                "--and clmline.source_type = 'Claims'\n" +
                "and (case when clmline.source_type = 'Claims' and clmline.aco_id = '3486' then SUBSTRING(clmline.source_file_name,20,4) else null end)=\n" +
                "(case when mem.source_type = 'Claims' and mem.aco_id = '3486' then SUBSTRING(mem.source_file_name,20,4) else null end)\n" +
                "\n" +
                "left join l2.member_altid alt\n" +
                "--ON clmline.empi=alt.empi\n" +
                "on clmline.local_member_id=alt.local_member_id\n" +
                "and clmline.birth_date=alt.birth_date\n" +
                "and clmline.source_type=alt.source_type\n" +
                "and clmline.aco_id = alt.aco_id \n" +
                "and clmline.source_type = 'Claims'\n" +
                "\n" +
                "/*left join l2.pd_npi as npi1\n" +
                "on clmline.servicing_provider_npi=npi1.npi*/\n" +
                "\n" +
                "where clmline.source_type = 'Claims' \n" +
                "and clmline.aco_id='3486'\n" +
                "and (case when clmline.source_type = 'Claims' and clmline.aco_id = '3486' and clmline.source_file_name like '%CCLF%' then rcdt = '2017-12-01' else FALSE end));");

        Statement statement = CCJSqlParserUtil.parse(insert.getSelect().toString());
        Select selectStatement = (Select) statement;
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(selectStatement);
        System.out.println(tableList);

    }
}
