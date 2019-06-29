package io.cdap.directives;

import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.RecipePipeline;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.test.TestingRig;
import io.cdap.wrangler.test.api.TestRecipe;
import io.cdap.wrangler.test.api.TestRows;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class XPathExpTest {

  private static final String XML = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
    "<POSLog xmlns=\"http://www.nrf-arts.org/IXRetail/namespace/\" xmlns:WN=\"http://www.wincor-nixdorf.com\">\n" +
    "  <Transaction>\n" +
    "    <RetailStoreID>502</RetailStoreID>\n" +
    "    <OrganizationHierarchy Level=\"Corporation\" ID=\"AE\">UAE</OrganizationHierarchy>\n" +
    "    <WorkstationID>2</WorkstationID>\n" +
    "    <SequenceNumber>2729</SequenceNumber>\n" +
    "    <BusinessDayDate>2018-07-14</BusinessDayDate>\n" +
    "    <BeginDateTime>2018-07-14T14:25:04</BeginDateTime>\n" +
    "    <EndDateTime>2018-07-14T14:25:39</EndDateTime>\n" +
    "    <OperatorID OperatorName=\"Fathima Kimkiman\">110219</OperatorID>\n" +
    "    <CurrencyCode>AED</CurrencyCode>\n" +
    "    <RetailTransaction Version=\"2.2\">\n" +
    "      <LineItem EntryMethod=\"Keyed\">\n" +
    "        <SequenceNumber>3</SequenceNumber>\n" +
    "        <CreateNumber>3</CreateNumber>\n" +
    "        <Sale ItemType=\"Stock\">\n" +
    "          <ItemID>32</ItemID>\n" +
    "          <POSIdentity POSIDType=\"EAN\">\n" +
    "            <POSItemID>0000000000121</POSItemID>\n" +
    "          </POSIdentity>\n" +
    "          <POSIdentity POSIDType=\"POSItemID\">\n" +
    "            <POSItemID>0000000000421</POSItemID>\n" +
    "          </POSIdentity>\n" +
    "          <MerchandiseHierarchy Level=\"MerchandiseHierarchyLevel\">0000</MerchandiseHierarchy>\n" +
    "          <MerchandiseHierarchy Level=\"POSDepartment\">003011001</MerchandiseHierarchy>\n" +
    "          <Description>Isa Shade Liner 1136</Description>\n" +
    "          <UnitListPrice>21</UnitListPrice>\n" +
    "          <RegularSalesUnitPrice>21</RegularSalesUnitPrice>\n" +
    "          <ActualSalesUnitPrice>21</ActualSalesUnitPrice>\n" +
    "          <ExtendedAmount>21</ExtendedAmount>\n" +
    "          <Quantity Units=\"1\" UnitOfMeasureCode=\"EA\">1</Quantity>\n" +
    "          <WN:POSDescription>Isa Shade Liner 1136-02 pc</WN:POSDescription>\n" +
    "          <WN:Brand>ISADORA</WN:Brand>\n" +
    "          <WN:QUOM>EA</WN:QUOM>\n" +
    "          <WN:Measurement>1</WN:Measurement>\n" +
    "          <WN:LocalStoreItem>false</WN:LocalStoreItem>\n" +
    "          <WN:ProdRangeCampaignID>VKP0</WN:ProdRangeCampaignID>\n" +
    "          <WN:MeterialType>NRML</WN:MeterialType>\n" +
    "        </Sale>\n" +
    "        <WN:CreateInfo>\n" +
    "          <WN:SalesChannelID>1</WN:SalesChannelID>\n" +
    "          <WN:RetailStoreID>502</WN:RetailStoreID>\n" +
    "          <WN:WorkstationID>2</WN:WorkstationID>\n" +
    "          <WN:WorkstationGroupID>Checkouts</WN:WorkstationGroupID>\n" +
    "          <WN:OperatorID OperatorName=\"Fathima Kimkiman\">110219</WN:OperatorID>\n" +
    "        </WN:CreateInfo>\n" +
    "      </LineItem>\n" +
    "      <LineItem EntryMethod=\"Keyed\" VoidFlag=\"true\">\n" +
    "        <SequenceNumber>4</SequenceNumber>\n" +
    "        <CreateNumber>8</CreateNumber>\n" +
    "        <CreateRefNumber>3</CreateRefNumber>\n" +
    "        <OperatorBypassApproval>\n" +
    "          <SequenceNumber>5</SequenceNumber>\n" +
    "          <ApproverID OperatorName=\"Jonalyn Ofialda Padasas\">112752</ApproverID>\n" +
    "          <Description>You don't have sufficient rights for this function. </Description>\n" +
    "        </OperatorBypassApproval>\n" +
    "        <Sale ItemType=\"Stock\">\n" +
    "          <ItemID>32</ItemID>\n" +
    "          <POSIdentity POSIDType=\"EAN\">\n" +
    "            <POSItemID>0000000000721</POSItemID>\n" +
    "          </POSIdentity>\n" +
    "          <POSIdentity POSIDType=\"POSItemID\">\n" +
    "            <POSItemID>0000000000393</POSItemID>\n" +
    "          </POSIdentity>\n" +
    "          <MerchandiseHierarchy Level=\"MerchandiseHierarchyLevel\">0000</MerchandiseHierarchy>\n" +
    "          <MerchandiseHierarchy Level=\"POSDepartment\">003011001</MerchandiseHierarchy>\n" +
    "          <Description>Isa Shade Liner 1136</Description>\n" +
    "          <UnitListPrice>21</UnitListPrice>\n" +
    "          <RegularSalesUnitPrice>21</RegularSalesUnitPrice>\n" +
    "          <ActualSalesUnitPrice>21</ActualSalesUnitPrice>\n" +
    "          <ExtendedAmount>21</ExtendedAmount>\n" +
    "          <Quantity Units=\"1\" UnitOfMeasureCode=\"EA\">1</Quantity>\n" +
    "          <ItemLink>3</ItemLink>\n" +
    "          <WN:POSDescription>Isa Shade Liner 1136-02 pc</WN:POSDescription>\n" +
    "          <WN:Brand>ISADORA</WN:Brand>\n" +
    "          <WN:QUOM>EA</WN:QUOM>\n" +
    "          <WN:Measurement>-1</WN:Measurement>\n" +
    "          <WN:LocalStoreItem>false</WN:LocalStoreItem>\n" +
    "          <WN:ProdRangeCampaignID>VKP0</WN:ProdRangeCampaignID>\n" +
    "          <WN:MeterialType>NRML</WN:MeterialType>\n" +
    "        </Sale>\n" +
    "        <WN:CreateInfo>\n" +
    "          <WN:SalesChannelID>1</WN:SalesChannelID>\n" +
    "          <WN:RetailStoreID>502</WN:RetailStoreID>\n" +
    "          <WN:WorkstationID>2</WN:WorkstationID>\n" +
    "          <WN:WorkstationGroupID>Checkouts</WN:WorkstationGroupID>\n" +
    "          <WN:OperatorID OperatorName=\"Fathima Kimkiman\">110219</WN:OperatorID>\n" +
    "        </WN:CreateInfo>\n" +
    "      </LineItem>\n" +
    "      <Total TotalType=\"TransactionNetAmount\" WN:DateTime=\"2018-07-14T14:25:38\">0</Total>\n" +
    "      <Total TotalType=\"TransactionTaxAmount\" WN:DateTime=\"2018-07-14T14:25:38\">0</Total>\n" +
    "      <Total TotalType=\"TransactionGrandAmount\" WN:DateTime=\"2018-07-14T14:25:38\">0</Total>\n" +
    "      <Total TotalType=\"RoundingAmount\" WN:DateTime=\"2018-07-14T14:25:38\">0</Total>\n" +
    "    </RetailTransaction>\n" +
    "    <WN:Country Code=\"AE\">\n" +
    "    </WN:Country>\n" +
    "    <WN:ExternalRetailStoreID>502</WN:ExternalRetailStoreID>\n" +
    "    <WN:SalesChannelID>1</WN:SalesChannelID>\n" +
    "    <WN:ExternalSalesChannelID>Sales</WN:ExternalSalesChannelID>\n" +
    "    <WN:PosVersion>5.5.2.102</WN:PosVersion>\n" +
    "    <WN:SessionID>502:2:2701:110219:20180712</WN:SessionID>\n" +
    "    <WN:WorkstationGroupID>Checkouts</WN:WorkstationGroupID>\n" +
    "    <WN:Version>5.0.0.0</WN:Version>\n" +
    "    <WN:Barcode>660502002000272920180714142504</WN:Barcode>\n" +
    "  </Transaction>\n" +
    "</POSLog>\n";

  private static String XML_SIMPLE = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
    "<inventory>\n" +
    "    <!--Test is test comment-->\n" +
    "        <book year=\"2000\">\n" +
    "        <title>Snow Crash</title>\n" +
    "        <author>Neal Stephenson</author>\n" +
    "        <publisher>Spectra</publisher>\n" +
    "        <isbn>0553380958</isbn>\n" +
    "        <price>14.95</price>\n" +
    "    </book>\n" +
    "    <book year=\"2005\">\n" +
    "        <title>Burning Tower</title>\n" +
    "        <author>Larry Niven</author>\n" +
    "        <author>Jerry Pournelle</author>\n" +
    "        <publisher>Pocket</publisher>\n" +
    "        <isbn>0743416910</isbn>\n" +
    "        <price>5.99</price>\n" +
    "    </book>\n" +
    "    <book year=\"1995\">\n" +
    "        <title>Zodiac</title>\n" +
    "        <author>Neal Stephenson</author>\n" +
    "        <publisher>Spectra</publisher>\n" +
    "        <isbn>0553573862</isbn>\n" +
    "        <price>7.50</price>\n" +
    "    </book>\n" +
    "</inventory>";

  @Test
  @Ignore
  public void testBasic() throws Exception {
    List<Class<? extends Directive>> directives = new ArrayList<>();
    TestRows rows = new TestRows();
    rows.add(new Row("body", XML));

    directives.add(XPathExpression.class);
    directives.add(XMLParser.class);

    TestRecipe recipe0 = new TestRecipe();
    recipe0.add("parse-as-xml :body");
    recipe0.add("xpath-exp :body '//:POSLog/:Transaction/:RetailTransaction/:LineItem[*]/:Sale' :sales");
    recipe0.add("drop body");

    RecipePipeline pipeline0 = TestingRig.pipeline(directives, recipe0);
    List<Row> actuals0 = pipeline0.execute(rows.toList());

    Assert.assertEquals(1, actuals0.size()); // Only one row
    Assert.assertEquals(1, actuals0.get(0).length()); // Only one column
  }

}