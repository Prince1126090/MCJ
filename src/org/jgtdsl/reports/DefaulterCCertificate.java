package org.jgtdsl.reports;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.util.ServletContextAware;
import org.jgtdsl.dto.AddressDTO;
import org.jgtdsl.dto.ClearnessDTO;
import org.jgtdsl.dto.CustomerApplianceDTO;
import org.jgtdsl.dto.CustomerConnectionDTO;
import org.jgtdsl.dto.CustomerDTO;
import org.jgtdsl.dto.CustomerPersonalDTO;
import org.jgtdsl.dto.ResponseDTO;
import org.jgtdsl.dto.UserDTO;
import org.jgtdsl.enums.ConnectionStatus;
import org.jgtdsl.enums.ConnectionType;
import org.jgtdsl.enums.MeteredStatus;
import org.jgtdsl.enums.Month;
import org.jgtdsl.enums.Area;
import org.jgtdsl.models.CustomerService;
import org.jgtdsl.models.MeterService;
import org.jgtdsl.utils.cache.CacheUtil;
import org.jgtdsl.utils.connection.ConnectionManager;
import org.jgtdsl.utils.connection.TransactionManager;

import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.opensymphony.xwork2.ActionSupport;

public class DefaulterCCertificate extends ActionSupport implements
		ServletContextAware {
	ClearnessDTO clearnessDTO = new ClearnessDTO();
	// ArrayList<ClearnessDTO> dueMonthList=new ArrayList<ClearnessDTO>();
	ClearnessDTO cto = new ClearnessDTO();

	private static final long serialVersionUID = 8854240739341830184L;
	private String customer_id;
	private String download_type;
	private String area;
	private String collection_month;
	private String from_customer_id;
	private String to_customer_id;
	private String customer_category;
	private String customer_type;
	private String calender_year;
	private String officer_name;
	private String officer_desig;
	private String certification_id;
	private String report_type;
	private ServletContext servlet;
	String yearsb;
	ArrayList<ClearnessDTO> CustomerList = new ArrayList<ClearnessDTO>();
	CustomerDTO customer = new CustomerDTO();
	ClearnessDTO customerInfo;
	MeterService ms = new MeterService();
	ArrayList<CustomerApplianceDTO> applianceList = new ArrayList<CustomerApplianceDTO>();
	DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyy");
	Date date = new Date();

	static DecimalFormat taka_format = new DecimalFormat("#,##,##,##,##,##0.00");
	static DecimalFormat consumption_format = new DecimalFormat(
			"##########0.000");

	UserDTO loggedInUser = (UserDTO) ServletActionContext.getRequest()
			.getSession().getAttribute("user");

	// Connection conn = ConnectionManager.getConnection();

	public String execute() throws Exception {
		HttpServletResponse response = ServletActionContext.getResponse();

		PdfReader reader = null;
		ByteArrayOutputStream certificate = null;
		List<PdfReader> readers = null;
		String realPathC = "";
		String realPathD = "";
		String picPath = "";
		Document document = new Document();
		ByteArrayOutputStream out = null;
		document.setPageSize(PageSize.A4);
		document.setMargins(10, 10, 10, 10);
		// left,right,top,bottom
		String fileName = "";
		readers = new ArrayList<PdfReader>();
		BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN,
				BaseFont.WINANSI, BaseFont.EMBEDDED);
		BaseFont bfb = BaseFont.createFont(BaseFont.TIMES_BOLD,
				BaseFont.WINANSI, BaseFont.EMBEDDED);

		try {

			picPath = servlet.getRealPath("/resources/images/logo.png");
			realPathC = servlet
					.getRealPath("/resources/staticPdf/CertificateC.pdf");
			realPathD = servlet
					.getRealPath("/resources/staticPdf/CertificateD.pdf");

			document = new Document();
			out = new ByteArrayOutputStream();
			// left,right,top,bottom
			fileName = "ClearnessCertificate.pdf";
			if (download_type.equalsIgnoreCase("individual_wise")) {

				/*
				 * customerInfo = getCustomerInfo(customer_id, area,
				 * calender_year, collection_month); customer =
				 * getCustomerInfo(customer_id);
				 */
				ClearnessDTO cDto = new ClearnessDTO();
				cDto.setCustomerID(customer_id);
				CustomerList.add(cDto);

			} else if (download_type.equalsIgnoreCase("category_wise")) {
				CustomerList = getCustomerList(from_customer_id,
						to_customer_id, customer_category, area);
			}

			for (int i = 0; i < CustomerList.size(); i++) {

				// ///////////

				if (download_type.equals("individual_wise")) {
					customerInfo = getCustomerInfo(customer_id, area,
							calender_year, collection_month);
					customer = getCustomerInfo(customer_id);
					certification_id = customer_id + collection_month
							+ calender_year;

					applianceList = ms.getCustomerApplianceList(customer_id);
				} else {

					customerInfo = getCustomerInfo(CustomerList.get(i)
							.getCustomerID(), area, calender_year,
							collection_month);
					certification_id = CustomerList.get(i).getCustomerID()
							+ collection_month + calender_year;
					customer = getCustomerInfo(CustomerList.get(i)
							.getCustomerID());
					applianceList = ms.getCustomerApplianceList(CustomerList
							.get(i).getCustomerID());
				}
				// //////////////

				yearsb = calender_year.substring(2, 4);
				PdfContentByte over = null;
				PdfStamper stamp = null;

				if (customerInfo.getDueAmount()==0
						|| customerInfo.getDueMonth().equals("")
						|| customerInfo.getCustomerID().equals(null)) {
					reader = new PdfReader(new FileInputStream(realPathC));
					certificate = new ByteArrayOutputStream();
					stamp = new PdfStamper(reader, certificate);
					over = stamp.getOverContent(1);

					over.beginText();
					// certification ID

					over.setFontAndSize(bfb, 8);
					over.showTextAligned(PdfContentByte.ALIGN_LEFT,
							certification_id, 95, 660, 0);
					over.showTextAligned(PdfContentByte.ALIGN_LEFT,
							certification_id, 95, 354, 0);

					// Date

					over.setFontAndSize(bfb, 8);
					over.showTextAligned(PdfContentByte.ALIGN_LEFT,
							dateFormat.format(date), 487, 660, 0);
					over.showTextAligned(PdfContentByte.ALIGN_LEFT,
							dateFormat.format(date), 487, 354, 0);
					// appliance

					if (applianceList.size() == 0) {
						over.setFontAndSize(bfb, 8);
						over.showTextAligned(PdfContentByte.ALIGN_LEFT, "N/A",
								265, 545, 0);
						over.showTextAligned(PdfContentByte.ALIGN_LEFT, "N/A",
								305, 545, 0);
						over.showTextAligned(PdfContentByte.ALIGN_LEFT, "N/A",
								345, 545, 0);

						over.showTextAligned(PdfContentByte.ALIGN_LEFT, "N/A",
								265, 242, 0);
						over.showTextAligned(PdfContentByte.ALIGN_LEFT, "N/A",
								305, 242, 0);
						over.showTextAligned(PdfContentByte.ALIGN_LEFT, "N/A",
								345, 242, 0);
					}

					for (CustomerApplianceDTO x : applianceList) {
						over.setFontAndSize(bfb, 8);
						if (x.getApplianc_id().equalsIgnoreCase("01")) {
							over.showTextAligned(PdfContentByte.ALIGN_LEFT, "0"
									+ x.getApplianc_qnt(), 265, 545, 0);
							over.showTextAligned(PdfContentByte.ALIGN_LEFT, "0"
									+ x.getApplianc_qnt(), 265, 242, 0);

						} else if (x.getApplianc_id().equalsIgnoreCase("02")) {
							over.showTextAligned(PdfContentByte.ALIGN_LEFT, "0"
									+ x.getApplianc_qnt(), 305, 545, 0);
							over.showTextAligned(PdfContentByte.ALIGN_LEFT, "0"
									+ x.getApplianc_qnt(), 305, 242, 0);

						} else {
							over.showTextAligned(PdfContentByte.ALIGN_LEFT, "0"
									+ x.getApplianc_qnt(), 345, 545, 0);
							over.showTextAligned(PdfContentByte.ALIGN_LEFT, "0"
									+ x.getApplianc_qnt(), 345, 242, 0);
						}
					}

					// month
					String month_name = (Month.values()[Integer
							.parseInt(collection_month) - 1].getLabel());

					over.setFontAndSize(bfb, 8);
					over.showTextAligned(PdfContentByte.ALIGN_LEFT, month_name
							+ " " + yearsb, 245, 645, 0);
					over.setFontAndSize(bfb, 8);
					over.showTextAligned(PdfContentByte.ALIGN_LEFT, month_name
							+ " " + yearsb, 245, 338, 0);
					// customer id
					over.setFontAndSize(bfb, 9);
					over.showTextAligned(PdfContentByte.ALIGN_LEFT,
							customer.getCustomer_id(), 100, 540, 0);
					over.setFontAndSize(bfb, 9);
					over.showTextAligned(PdfContentByte.ALIGN_LEFT,
							customer.getCustomer_id(), 100, 232, 0);

					// name address

					over.setFontAndSize(bfb, 6);
					over.showTextAligned(PdfContentByte.ALIGN_LEFT,
							customer.getCustomer_name(), 115, 453, 0);
					over.setFontAndSize(bfb, 6);
					over.showTextAligned(PdfContentByte.ALIGN_LEFT,
							customer.getCustomer_name(), 115, 147, 0);

					over.setFontAndSize(bf, 6);
					over.showTextAligned(PdfContentByte.ALIGN_LEFT,
							customer.getAddress(), 115, 444, 0);
					over.setFontAndSize(bf, 6);
					over.showTextAligned(PdfContentByte.ALIGN_LEFT,
							customer.getAddress(), 115, 138, 0);

					// Signature
					over.setFontAndSize(bf, 7);
					over.showTextAligned(PdfContentByte.ALIGN_LEFT,
							officer_name + ", " + officer_desig, 457, 462, 0);
					over.setFontAndSize(bf, 7);
					over.showTextAligned(PdfContentByte.ALIGN_LEFT,
							officer_name + ", " + officer_desig, 457, 156, 0);

					// /////////////////////////////////for
					// defaulter//////////////////////////////////////

				} else {
					reader = new PdfReader(new FileInputStream(realPathD));
					certificate = new ByteArrayOutputStream();
					stamp = new PdfStamper(reader, certificate);
					over = stamp.getOverContent(1);

					over.beginText();

					// certification ID

					over.setFontAndSize(bfb, 8);
					over.showTextAligned(PdfContentByte.ALIGN_LEFT,
							certification_id, 95, 660, 0);
					over.showTextAligned(PdfContentByte.ALIGN_LEFT,
							certification_id, 95, 357, 0);

					// date
					DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyy");
					Date date = new Date();
					over.setFontAndSize(bfb, 8);
					over.showTextAligned(PdfContentByte.ALIGN_LEFT,
							dateFormat.format(date), 487, 660, 0);
					over.showTextAligned(PdfContentByte.ALIGN_LEFT,
							dateFormat.format(date), 487, 357, 0);
					// customer Id
					over.setFontAndSize(bfb, 8);
					over.showTextAligned(PdfContentByte.ALIGN_LEFT,
							customerInfo.getCustomerID(), 190, 645, 0);
					over.setFontAndSize(bfb, 8);
					over.showTextAligned(PdfContentByte.ALIGN_LEFT,
							customerInfo.getCustomerID(), 190, 342, 0);
					// month
					String month_name = (Month.values()[Integer
							.parseInt(collection_month) - 1].getLabel());
					over.setFontAndSize(bfb, 8);
					over.showTextAligned(PdfContentByte.ALIGN_LEFT, month_name
							+ " " + yearsb, 270, 645, 0);
					over.setFontAndSize(bfb, 8);
					over.showTextAligned(PdfContentByte.ALIGN_LEFT, month_name
							+ " " + yearsb, 270, 342, 0);
					// due month
					over.setFontAndSize(bf, 5);

					String hsi = customerInfo.getDueMonth();
					if (customerInfo.getDueMonth() != null)
						hsi = customerInfo.getDueMonth().replaceAll("&#x26;",
								"&");
					int size = 65;
					if (hsi != null && hsi.length() > size) {
						String[] s1;
						s1 = spitSrting(hsi, size);

						if (s1[1].length() <= size) {
							over.setTextMatrix(75, 565);
							over.showText(s1[0]);
							over.setTextMatrix(75, 262);
							over.showText(s1[0]);
						} else {
							s1 = spitSrting(s1[1], size);
							over.setTextMatrix(75, 565);
							over.showText(s1[0]);
							over.setTextMatrix(75, 262);
							over.showText(s1[0]);

							if (s1[1].length() <= size) {
								over.setTextMatrix(75, 555);
								over.showText(s1[1]);
								over.setTextMatrix(75, 252);
								over.showText(s1[1]);
							} else {
								s1 = spitSrting(s1[1], size);
								over.setTextMatrix(75, 555);
								over.showText(s1[0]);
								over.setTextMatrix(75, 252);
								over.showText(s1[0]);
								over.setTextMatrix(75, 545);
								if (s1[1].length() > size)
									over.showText(s1[1].substring(size));
								else
									over.showText(s1[1]);
								over.setTextMatrix(75, 242);
								if (s1[1].length() > size)
									over.showText(s1[1].substring(size));
								else
									over.showText(s1[1]);
							}
						}
					} else {
						over.setTextMatrix(75, 565);
						over.showText(hsi);
						over.setTextMatrix(75, 262);
						over.showText(hsi);
					}
					// appliance
					if (applianceList.size() == 0) {
						over.setFontAndSize(bfb, 8);
						over.showTextAligned(PdfContentByte.ALIGN_LEFT, "N/A",
								260, 555, 0);
						over.showTextAligned(PdfContentByte.ALIGN_LEFT, "N/A",
								300, 555, 0);
						over.showTextAligned(PdfContentByte.ALIGN_LEFT, "N/A",
								340, 555, 0);

						over.showTextAligned(PdfContentByte.ALIGN_LEFT, "N/A",
								260, 252, 0);
						over.showTextAligned(PdfContentByte.ALIGN_LEFT, "N/A",
								300, 252, 0);
						over.showTextAligned(PdfContentByte.ALIGN_LEFT, "N/A",
								340, 252, 0);
					}

					for (CustomerApplianceDTO x : applianceList) {
						over.setFontAndSize(bfb, 8);
						if (x.getApplianc_id().equalsIgnoreCase("01")) {
							over.showTextAligned(PdfContentByte.ALIGN_LEFT, "0"
									+ x.getApplianc_qnt(), 260, 555, 0);
							over.showTextAligned(PdfContentByte.ALIGN_LEFT, "0"
									+ x.getApplianc_qnt(), 260, 252, 0);

						} else if (x.getApplianc_id().equalsIgnoreCase("02")) {
							over.showTextAligned(PdfContentByte.ALIGN_LEFT, "0"
									+ x.getApplianc_qnt(), 300, 555, 0);
							over.showTextAligned(PdfContentByte.ALIGN_LEFT, "0"
									+ x.getApplianc_qnt(), 300, 252, 0);

						} else {
							over.showTextAligned(PdfContentByte.ALIGN_LEFT, "0"
									+ x.getApplianc_qnt(), 340, 555, 0);
							over.showTextAligned(PdfContentByte.ALIGN_LEFT, "0"
									+ x.getApplianc_qnt(), 340, 252, 0);
						}
					}
					// due amount
					over.setFontAndSize(bfb, 8);
					over.showTextAligned(PdfContentByte.ALIGN_LEFT,
							String.valueOf(customerInfo.getDueAmount()), 400,
							555, 0);
					over.setFontAndSize(bfb, 8);
					over.showTextAligned(PdfContentByte.ALIGN_LEFT,
							String.valueOf(customerInfo.getDueAmount()), 400,
							252, 0);
					// area name
					over.setFontAndSize(bfb, 6);
					over.showTextAligned(PdfContentByte.ALIGN_LEFT, Area
							.values()[Integer.parseInt(area) - 1].getLabel(),
							100, 498, 0);
					over.setFontAndSize(bfb, 6);
					over.showTextAligned(PdfContentByte.ALIGN_LEFT, Area
							.values()[Integer.parseInt(area) - 1].getLabel(),
							100, 195, 0);

					// name address
					over.setFontAndSize(bfb, 6);
					over.showTextAligned(PdfContentByte.ALIGN_LEFT,
							customerInfo.getCustomerName(), 115, 458, 0);
					over.setFontAndSize(bfb, 6);
					over.showTextAligned(PdfContentByte.ALIGN_LEFT,
							customerInfo.getCustomerName(), 115, 155, 0);

					over.setFontAndSize(bf, 6);
					over.showTextAligned(PdfContentByte.ALIGN_LEFT,
							customerInfo.getCustomerAddress(), 115, 448, 0);
					over.setFontAndSize(bf, 6);
					over.showTextAligned(PdfContentByte.ALIGN_LEFT,
							customerInfo.getCustomerAddress(), 115, 145, 0);

					// in words
					over.setFontAndSize(bfb, 8);
					over.showTextAligned(PdfContentByte.ALIGN_LEFT,
							customerInfo.getAmountInWords(), 95, 523, 0);

					over.showTextAligned(PdfContentByte.ALIGN_LEFT,
							customerInfo.getAmountInWords(), 95, 221, 0);
					// signature
					over.setFontAndSize(bf, 7);
					over.showTextAligned(PdfContentByte.ALIGN_LEFT,
							officer_name + ", " + officer_desig, 457, 468, 0);
					over.setFontAndSize(bf, 7);
					over.showTextAligned(PdfContentByte.ALIGN_LEFT,
							officer_name + ", " + officer_desig, 457, 165, 0);
				}
				// / website and email
				over.setFontAndSize(bf, 6);
				over.showTextAligned(PdfContentByte.ALIGN_LEFT,
						"www.jalalabadgas.org.bd", 50, 750, 0);
				over.setFontAndSize(bf, 6);
				over.showTextAligned(PdfContentByte.ALIGN_LEFT,
						"www.jalalabadgas.org.bd", 50, 50, 0);

				over.showTextAligned(PdfContentByte.ALIGN_LEFT,
						"Email: md@jalalabadgas.org.bd", 480, 750, 0);
				over.setFontAndSize(bf, 6);
				over.showTextAligned(PdfContentByte.ALIGN_LEFT,
						"Email: md@jalalabadgas.org.bd", 480, 50, 0);

				over.endText();
				stamp.close();
				readers.add(new PdfReader(certificate.toByteArray()));
				insertClarificationHistory(customer.getCustomer_id(),
						dateFormat.format(date), officer_name,
						customerInfo.getDueAmount());
			}
			if (readers.size() > 0) {
				PdfWriter writer = PdfWriter.getInstance(document, out);

				document.open();

				PdfContentByte cb = writer.getDirectContent();
				PdfReader pdfReader = null;
				PdfImportedPage page;

				for (int k = 0; k < readers.size(); k++) {
					document.newPage();
					pdfReader = readers.get(k);
					page = writer.getImportedPage(pdfReader, 1);
					cb.addTemplate(page, 0, 0);
				}
				document.close();
				ReportUtil rptUtil = new ReportUtil();
				rptUtil.downloadPdf(out, response, fileName);
				document = null;

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private ClearnessDTO getCustomerInfo(String customer_id, String area_id,
			String year, String month) {
		Connection conn = ConnectionManager.getConnection();
		Statement st = null;
		ResultSet resultSet = null;
		ClearnessDTO ctrInfo = new ClearnessDTO();
		String type = customer_id.substring(0, 4);
		String bill_table;
		if (type.equalsIgnoreCase(area_id + "01")
				|| type.equalsIgnoreCase(area_id + "09")) {
			bill_table = "BILL_NON_METERED";
		} else {
			bill_table = "BILL_METERED";
		}

		try {

			String customer_info_sql = "SELECT * "
					+ "  FROM (  SELECT bi.CUSTOMER_ID, "
					+ "                 CUSTOMER_CATEGORY, "
					+ "                 bi.AREA_ID, "
					+ "                 LISTAGG ( "
					+ "                       TO_CHAR (TO_DATE (BILL_MONTH, 'MM'), 'MON') "
					+ "                    || ' ' "
					+ "                    || SUBSTR (BILL_YEAR, 3), "
					+ "                    ',') "
					+ "                 WITHIN GROUP (ORDER BY BILL_YEAR ASC, BILL_MONTH ASC) "
					+ "                    AS DUEMONTH, "
					+ "                 SUM ( "
					+ "                      BILLED_AMOUNT "
					+ "                    + CALCUALTESURCHARGE (BILL_ID, "
					+ "                                          TO_CHAR (SYSDATE, 'dd-mm-YYYY'))) "
					+ "                    totalamount, "
					+ " NUMBER_SPELLOUT_FUNC ( "
					+ "                    TO_NUMBER ( "
					+ "                       SUM ( "
					+ "                            BILLED_AMOUNT "
					+ "                          + CALCUALTESURCHARGE ( "
					+ "                               BILL_ID, "
					+ "                               TO_CHAR (SYSDATE, 'dd-mm-YYYY'))), "
					+ "                       '99999999999999.99')) "
					+ "                    inwords, "
					+ "                 COUNT (*) cnt " + "            FROM "
					+ bill_table
					+ " bi, CUSTOMER_CONNECTION cc "
					+ "           WHERE     BI.CUSTOMER_ID = CC.CUSTOMER_ID "
					+ "                 AND CC.STATUS = 1 "
					+ "                 AND bi.STATUS = 1 "
					+ "                 AND bi.area_id = '"
					+ this.area
					+ "' "
					+ " AND BI.CUSTOMER_ID = '"
					+ customer_id
					+ "' "

					// "                 And bi.CUSTOMER_CATEGORY= " +
					+ "                 AND BILL_YEAR || LPAD (BILL_MONTH, 2, 0) <= '"
					+ year
					+ month
					+ "' "
					+ "        GROUP BY BI.CUSTOMER_ID, CUSTOMER_CATEGORY, bi.AREA_ID "
					+ "          HAVING COUNT (*) >= 1) tmp1, "
					+ "       (SELECT AA.CUSTOMER_ID, "
					+ "               BB.FULL_NAME, "
					+ "               BB.MOBILE, "
					+ "               AA.ADDRESS_LINE1, "
					+ "               AA.ADDRESS_LINE2 "
					+ "          FROM CUSTOMER_ADDRESS aa, CUSTOMER_PERSONAL_INFO bb "
					+ "         WHERE AA.CUSTOMER_ID = BB.CUSTOMER_ID) tmp2 "
					+ " WHERE tmp1.CUSTOMER_ID = tmp2.CUSTOMER_ID ";

			st = conn.createStatement();// Statement(customer_info_sql);

			resultSet = st.executeQuery(customer_info_sql);

			while (resultSet.next()) {

				ctrInfo.setCustomerID(resultSet.getString("CUSTOMER_ID"));
				ctrInfo.setCustomerName(resultSet.getString("FULL_NAME"));
				ctrInfo.setCustomerAddress(resultSet.getString("ADDRESS_LINE1"));
				ctrInfo.setDueMonth(resultSet.getString("DUEMONTH"));
				ctrInfo.setDueAmount(resultSet.getDouble("TOTALAMOUNT"));
				ctrInfo.setArea(resultSet.getString("AREA_ID"));
				ctrInfo.setAmountInWords(resultSet.getString("INWORDS"));
			}
		} catch (SQLException e) {

			e.printStackTrace();
		} finally {
			try {
				st.close();
				resultSet.close();
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return ctrInfo;
	}

	public ClearnessDTO getClearnessDTO() {
		return clearnessDTO;
	}

	public String getReport_type() {
		return report_type;
	}

	public void setReport_type(String report_type) {
		this.report_type = report_type;
	}

	public void setClearnessDTO(ClearnessDTO clearnessDTO) {
		this.clearnessDTO = clearnessDTO;
	}

	public ClearnessDTO getCto() {
		return cto;
	}

	public String getCertification_id() {
		return certification_id;
	}

	public void setCertification_id(String certification_id) {
		this.certification_id = certification_id;
	}

	public void setCto(ClearnessDTO cto) {
		this.cto = cto;
	}

	public String getCustomer_id() {
		return customer_id;
	}

	public void setCustomer_id(String customer_id) {
		this.customer_id = customer_id;
	}

	public String getDownload_type() {
		return download_type;
	}

	public void setDownload_type(String download_type) {
		this.download_type = download_type;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getCollection_month() {
		return collection_month;
	}

	public void setCollection_month(String collection_month) {
		this.collection_month = collection_month;
	}

	public String getFrom_customer_id() {
		return from_customer_id;
	}

	public void setFrom_customer_id(String from_customer_id) {
		this.from_customer_id = from_customer_id;
	}

	public String getTo_customer_id() {
		return to_customer_id;
	}

	public void setTo_customer_id(String to_customer_id) {
		this.to_customer_id = to_customer_id;
	}

	public String getCustomer_category() {
		return customer_category;
	}

	public void setCustomer_category(String customer_category) {
		this.customer_category = customer_category;
	}

	public String getCustomer_type() {
		return customer_type;
	}

	public void setCustomer_type(String customer_type) {
		this.customer_type = customer_type;
	}

	public String getCalender_year() {
		return calender_year;
	}

	public void setCalender_year(String calender_year) {
		this.calender_year = calender_year;
	}

	public String getOfficer_name() {
		return officer_name;
	}

	public void setOfficer_name(String officer_name) {
		this.officer_name = officer_name;
	}

	public String getOfficer_desig() {
		return officer_desig;
	}

	public void setOfficer_desig(String officer_desig) {
		this.officer_desig = officer_desig;
	}

	public ServletContext getServlet() {
		return servlet;
	}

	public void setServlet(ServletContext servlet) {
		this.servlet = servlet;
	}

	public static DecimalFormat getTaka_format() {
		return taka_format;
	}

	public static void setTaka_format(DecimalFormat taka_format) {
		DefaulterCCertificate.taka_format = taka_format;
	}

	public static DecimalFormat getConsumption_format() {
		return consumption_format;
	}

	public static void setConsumption_format(DecimalFormat consumption_format) {
		DefaulterCCertificate.consumption_format = consumption_format;
	}

	public UserDTO getLoggedInUser() {
		return loggedInUser;
	}

	public void setLoggedInUser(UserDTO loggedInUser) {
		this.loggedInUser = loggedInUser;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public void setServletContext(ServletContext servlet) {
		this.servlet = servlet;
	}

	public String[] spitSrting(String base, int size) {
		char[] separator = { ' ', '.', ',', ';', ':' };
		boolean separatorfound = false;
		String s1[] = new String[2];
		outer: for (int j = size; j >= 0; j--) {
			for (int k = 0; k < separator.length; k++) {
				if (separator[k] == base.charAt(j)) {
					s1[0] = base.substring(0, j + 1);
					s1[1] = base.substring(j + 1, base.length());
					separatorfound = true;
					break outer;
				}
			}

		}
		if (!separatorfound) {
			int x = 0;
			s1[0] = base.substring(0, size - 10);
			s1[1] = base.substring(size - 10, base.length());
		}
		return s1;
	}

	public CustomerDTO getCustomerInfo(String customer_id) {
		CustomerDTO customer = null;

		Connection conn = ConnectionManager.getConnection();

		String sql = " Select * From MVIEW_CUSTOMER_INFO Where Customer_Id=? ";

		PreparedStatement stmt = null;
		ResultSet r = null;

		try {
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, customer_id);
			r = stmt.executeQuery();

			if (r.next()) {
				customer = new CustomerDTO();
				customer.setCustomer_id(r.getString("CUSTOMER_ID"));
				customer.setAddress(r.getString("ADDRESS"));
				customer.setCustomer_name(r.getString("FULL_NAME"));
			}
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			try {
				stmt.close();
				ConnectionManager.closeConnection(conn);
			} catch (Exception e) {
				e.printStackTrace();
			}
			stmt = null;
			conn = null;
		}

		return customer;

	}

	// /get multiple customer list
	private ArrayList<ClearnessDTO> getCustomerList(String from_cus_id,
			String to_cus_id, String cust_cat_id, String area) {

		ArrayList<ClearnessDTO> custList = new ArrayList<ClearnessDTO>();
		PreparedStatement ps1 = null;
		ResultSet resultSet = null;
		Connection conn = null;
		if (collection_month.length() < 2) {
			collection_month = "0" + collection_month;
		}
		String type = null;
		if (from_cus_id.isEmpty()) {
			type = area + this.customer_category;
		} else {
			type = from_cus_id.substring(0, 4);
		}
		String bill_table;
		if (type.equalsIgnoreCase(area + "01")
				|| type.equalsIgnoreCase(area + "09")) {
			bill_table = "BILL_NON_METERED";
		} else {
			bill_table = "BILL_METERED";
		}
		String whereClause = null;
		if (from_cus_id.isEmpty() && to_cus_id.isEmpty()) {
			whereClause = "      AND BI.CUSTOMER_CATEGORY='"
					+ this.customer_category + "'  ";
		} else {
			whereClause = "         AND BI.CUSTOMER_ID BETWEEN '" + from_cus_id
					+ "' AND '" + to_cus_id + "' ";
		}
		try {
			String transaction_sql = "  SELECT bi.CUSTOMER_ID, COUNT (*) cnt "
					+ "    FROM "
					+ bill_table
					+ " bi, CUSTOMER_CONNECTION cc "
					+ "   WHERE     BI.CUSTOMER_ID = CC.CUSTOMER_ID "
					+ "         AND CC.STATUS = 1 "
					+
					// "         AND bi.STATUS = 1 " +
					"         AND bi.area_id = '"
					+ area
					+ "' "
					+ whereClause
					+ "                 AND BILL_YEAR || LPAD (BILL_MONTH, 2, 0) <= '"
					+ calender_year
					+ collection_month
					+ "'  GROUP BY BI.CUSTOMER_ID, CUSTOMER_CATEGORY, bi.AREA_ID "
					+ "  HAVING COUNT (*) >= 1 ";

			conn = ConnectionManager.getConnection();
			ps1 = conn.prepareStatement(transaction_sql);
			resultSet = ps1.executeQuery();
			while (resultSet.next()) {
				ClearnessDTO ClearnessDTO = new ClearnessDTO();
				ClearnessDTO.setCustomerID(resultSet.getString("CUSTOMER_ID"));
				custList.add(ClearnessDTO);
			}

		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			try {
				ps1.close();
				resultSet.close();
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return custList;

	}

	// insert into database
	public void insertClarificationHistory(String cust_id, String issue_date,
			String insert_by, Double dues_status) {
		ResponseDTO response = new ResponseDTO();

		if (collection_month.length() < 2) {
			collection_month = "0" + collection_month;
		}
		TransactionManager transactionManager = new TransactionManager();
		Connection conn = transactionManager.getConnection();

		// response=validateReconnInfo(reconn,disconn);
		// if(response.isResponse()==false)
		// return response;

		String sqlInsert = "INSERT INTO CLARIFICATION_HISTORY ( "
				+ "   CUSTOMER_ID, CALENDER_YEAR, ISSUE_DATE,  "
				+ "   STATUS, DUES_STATUS, INSERTED_ON,  "
				+ "   INSERTED_BY, CALENDER_MONTH, CERTIFICATION_ID )  "
				+ "   VALUES ( ?,?,sysdate,?,?,sysdate,?,?,?)";

		String checkIsAvailable = "Select count(customer_id) CUS_COUNT from CLARIFICATION_HISTORY where CALENDER_MONTH=? and CALENDER_YEAR=? and customer_id=?";

		PreparedStatement stmt = null;
		ResultSet r = null;
		int count = 0;

		try {
			stmt = conn.prepareStatement(checkIsAvailable);
			stmt.setString(1, collection_month);
			stmt.setString(2, calender_year);
			stmt.setString(3, cust_id);
			r = stmt.executeQuery();
			if (r.next())
				count = r.getInt("CUS_COUNT");

			if (count == 0) {
				stmt = conn.prepareStatement(sqlInsert);
				stmt.setString(1, cust_id);
				stmt.setString(2, calender_year);
				stmt.setInt(3, 1); // / 1 means all generated(approved)
				stmt.setDouble(4, dues_status);
				stmt.setString(5, insert_by);
				stmt.setString(6, collection_month);
				stmt.setString(7, certification_id);
				stmt.execute();
			}
			transactionManager.commit();
		}

		catch (Exception e) {
			response.setMessasge(e.getMessage());
			response.setResponse(false);
			e.printStackTrace();
			try {
				transactionManager.rollback();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} finally {
			try {
				stmt.close();
				transactionManager.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			stmt = null;
			conn = null;
		}

		return;
	}
}
