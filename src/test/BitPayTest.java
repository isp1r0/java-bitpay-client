package test;

import static org.junit.Assert.*;

import java.security.interfaces.ECKey;
import java.util.List;

import controller.KeyUtils;
import model.Invoice;
import model.InvoiceBuyer;
import model.Rate;
import model.Rates;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import controller.BitPay;
import controller.BitPayException;

public class BitPayTest {

    private BitPay bitpay;
    private Invoice basicInvoice;
    private static double BTC_EPSILON = .000000001;
    private static double EPSILON = .001;

    private static String pairingCode = "FbaJTGU";
    private static String clientName = "BitPay Java Library Tester";
	
	@Before
	public void setUp() throws Exception 
	{
        // This scenario qualifies that this (test) client does not have merchant facade access.
		clientName += " on " + java.net.InetAddress.getLocalHost();
        com.google.bitcoin.core.ECKey key = KeyUtils.createEcKeyFromHexStringFile("locals/key");
		String testUrl = "https://staging.b-pay.net/";
        bitpay = new BitPay(key, clientName, testUrl );
        if (!bitpay.clientIsAuthorized(BitPay.FACADE_POS))
        {
            // Get POS facade authorization.
            // Obtain a pairingCode from your BitPay account administrator.  When the pairingCode
            // is created by your administrator it is assigned a facade.  To generate invoices a
            // POS facade is required.
            bitpay.authorizeClient(pairingCode);
        }		
	}

	@After
	public void tearDown() throws Exception 
	{
	}

	@Test
	public void testShouldGetInvoiceId() 
	{
        Invoice invoice = new Invoice(50.0, "USD");
		try {
			basicInvoice = bitpay.createInvoice(invoice);
		} catch (BitPayException e) {
			e.printStackTrace();
		}
		assertNotNull(basicInvoice.getId());
	}
	
	@Test
	public void testShouldGetInvoiceURL() 
	{
        Invoice invoice = new Invoice(50.0, "USD");
		try {
			basicInvoice = bitpay.createInvoice(invoice);
		} catch (BitPayException e) {
			e.printStackTrace();
		}
		assertNotNull(basicInvoice.getUrl());
	}

	@Test
	public void testShouldGetInvoiceStatus() 
	{
        Invoice invoice = new Invoice(50.0, "USD");
		try {
			basicInvoice = bitpay.createInvoice(invoice);
		} catch (BitPayException e) {
			e.printStackTrace();
		}
		assertEquals(Invoice.STATUS_NEW, basicInvoice.getStatus());
	}

	@Test
	public void testShouldGetInvoiceBTCPrice() 
	{
        Invoice invoice = new Invoice(50.0, "USD");
		try {
			basicInvoice = bitpay.createInvoice(invoice);
		} catch (BitPayException e) {
			e.printStackTrace();
		}
		assertNotNull(basicInvoice.getBtcPrice());		
	}

	@Test
	public void testShouldCreateInvoiceOneTenthBTC() 
	{
        Invoice invoice = new Invoice(0.1, "BTC");
		try {
			invoice = this.bitpay.createInvoice(invoice);
		} catch (BitPayException e) {
			e.printStackTrace();
		}
		assertEquals(0.1, invoice.getPrice(), BTC_EPSILON);
	}

	@Test
	public void testShouldCreateInvoice100USD() 
	{
        Invoice invoice = new Invoice(100.0, "USD");
		try {
			invoice = this.bitpay.createInvoice(invoice);
		} catch (BitPayException e) {
			e.printStackTrace();
		}
		assertEquals(100.0, invoice.getPrice(), EPSILON);
		
	}		
	
	@Test
	public void testShouldCreateInvoice100EUR() 
	{
        Invoice invoice = new Invoice(100.0, "EUR");
		try {
			invoice = this.bitpay.createInvoice(invoice);
		} catch (BitPayException e) {
			e.printStackTrace();
		}
		assertEquals(100.0, invoice.getPrice(), EPSILON);
	}
	
	@Test
	public void testShouldGetInvoice() 
	{
        Invoice invoice = new Invoice(100.0, "EUR");
		Invoice retreivedInvoice = null;
		try {
			invoice = this.bitpay.createInvoice(invoice);
			retreivedInvoice = this.bitpay.getInvoice(invoice.getId());
		} catch (BitPayException e) {
			e.printStackTrace();
		}
		assertEquals(invoice.getId(), retreivedInvoice.getId());		
	}

	@Test
	public void testShouldCreateInvoiceWithAdditionalParams() 
	{
		InvoiceBuyer buyer = new InvoiceBuyer();
		buyer.setName("Satoshi");
		buyer.setEmail("satoshi@bitpay.com");
		
        Invoice invoice = new Invoice(100.0, "USD");
        invoice.setBuyer(buyer);
        invoice.setFullNotifications(true);
        invoice.setNotificationEmail("satoshi@bitpay.com");
		invoice.setPosData("ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890");
		try {
	        invoice = this.bitpay.createInvoice(invoice);
		} catch (BitPayException e) {
			e.printStackTrace();
		}
		assertEquals(Invoice.STATUS_NEW, invoice.getStatus());
		assertEquals(100.0, invoice.getPrice(), EPSILON);
		assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890", invoice.getPosData());
		assertEquals("Satoshi", invoice.getBuyer().getName());
		assertEquals("satoshi@bitpay.com", invoice.getBuyer().getEmail());
		assertEquals(true, invoice.getFullNotifications());
		assertEquals("satoshi@bitpay.com", invoice.getNotificationEmail());
	}
	
	@Test
	public void testShouldGetExchangeRates() 
	{
		Rates rates = null;
		List<Rate> rateList = null;
		try {
			rates = this.bitpay.getRates();
			rateList = rates.getRates();		
		} catch (BitPayException e) {
			e.printStackTrace();
		}
		assertNotNull(rateList);
	}
	
	@Test
	public void testShouldGetEURExchangeRate() 
	{
		Rates rates = null;
		double rate = 0.0;
		try {
			rates = this.bitpay.getRates();
			rate = rates.getRate("EUR");
		} catch (BitPayException e) {
			e.printStackTrace();
		}
		assertTrue(rate != 0);
	}
	
	@Test
	public void testShouldGetCNYExchangeRate() 
	{
		Rates rates = null;
		double rate = 0.0;
		try {
			rates = this.bitpay.getRates();
			rate = rates.getRate("CNY");
		} catch (BitPayException e) {
			e.printStackTrace();
		}
		assertTrue(rate != 0);
	}

	@Test
	public void testShouldUpdateExchangeRates() 
	{
		Rates rates = null;
		List<Rate> rateList = null;
		try {
			rates = this.bitpay.getRates();
			rates.update();
			rateList = rates.getRates();
		} catch (BitPayException e) {
			e.printStackTrace();
		}
		assertNotNull(rateList);
	}

	/*
	@Test
	public void testShouldGetInvoices() {
		List<Invoice> invoices = null;
		try {
			invoices = this.bitpay.getInvoices("2014-01-01", "2014-09-01");
		} catch (BitPayException e) {
			e.printStackTrace();
		}
		assertTrue(invoices.size() > 0);
	}
	*/
}
