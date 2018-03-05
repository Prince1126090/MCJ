package org.jgtdsl.reports;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.util.ServletContextAware;
import org.jgtdsl.actions.BaseAction;
import org.jgtdsl.dto.CustomerApplianceDTO;
import org.jgtdsl.dto.MBillDTO;
import org.jgtdsl.dto.UserDTO;
import org.jgtdsl.enums.Area;
import org.jgtdsl.enums.Month;
import org.jgtdsl.models.BillingService;
import org.jgtdsl.models.MeterService;
import org.jgtdsl.utils.connection.ConnectionManager;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

public class BillNonMeteredCustomer extends BaseAction implements
		ServletContextAware {
	private static final long serialVersionUID = 8854240739341830184L;
	private ServletContext servlet;
	private String bill_id;
	private String customer_category;
	private String area_id;
	private String customer_id;
	private String category_name;
	private String bill_month;
	private String bill_year;
	private String download_type;
	private String bill_for;
	private int serial = 0;
	public static int textDiff = 597;
	private boolean water_mark = false;
	String c;

	public ArrayList<MBillDTO> getBill(String bill_id, String download_type) {
		Connection conn = ConnectionManager.getConnection();
		MBillDTO bill = null;
		ArrayList<MBillDTO> billList = new ArrayList<MBillDTO>();
		String where_condition = null;
		if (download_type.equalsIgnoreCase("S")) {
			where_condition = "WHERE bnm.bill_id = '" + bill_id + "' ";
		}else if(download_type.equalsIgnoreCase("M")){
			where_condition = "WHERE bnm.bill_id in("+bill_id+")";
		}
		String sql = 
				"SELECT BILL_ID, " +
				"       BILL_MONTH, " +
				"       BILL_YEAR, " +
				"       CUSTOMER_ID, " +
				"       AREA_ID, " +
				"       CUSTOMER_NAME, " +
				"       PROPRIETOR_NAME, " +
				"       CUSTOMER_CATEGORY_NAME, " +
				"       AREA_NAME, " +
				"       ADDRESS, " +
				"       BILLED_AMOUNT, " +
				"       TO_CHAR (BILL_GENERATION_DATE, 'dd-MM-YYYY') BILL_GENERATION_DATE, " +
				"       TO_CHAR (DUE_DATE, 'dd-MM-YYYY') DUE_DATE, " +
				"       NUMBER_SPELLOUT_FUNC (BILLED_AMOUNT) TAKA " +
				"  FROM bill_non_metered bnm "+ where_condition;

		try {
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(sql);

			while (rs.next()) {
				bill = new MBillDTO();
				bill.setBill_month(rs.getInt("BILL_MONTH"));
				bill.setBill_year(rs.getInt("BILL_YEAR"));
				bill.setCustomer_id(rs.getString("CUSTOMER_ID"));
				bill.setCustomer_name(rs.getString("CUSTOMER_NAME"));
				bill.setProprietor_name(rs.getString("PROPRIETOR_NAME"));
				bill.setCustomer_category_name(rs
						.getString("CUSTOMER_CATEGORY_NAME"));
				bill.setArea_name(rs.getString("AREA_NAME"));
				bill.setArea_id(rs.getString("AREA_ID"));
				bill.setAddress(rs.getString("ADDRESS"));
				bill.setBilled_amount(rs.getString("BILLED_AMOUNT"));
				bill.setIssue_date(rs.getString("BILL_GENERATION_DATE"));
				bill.setLast_pay_date_w_sc(rs.getString("DUE_DATE"));
				bill.setAmount_in_word(rs.getString("TAKA"));
				bill.setBill_month_name(Month.values()[rs.getInt("BILL_MONTH") - 1]
						.getLabel());
				billList.add(bill);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return billList;
	}

	public String downloadBill() throws Exception {

		Font font1 = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
		Font font2 = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);
		Font font3 = new Font(Font.FontFamily.HELVETICA, 6, Font.NORMAL);	

		UserDTO loggedInUser = (UserDTO) session.get("user");
		if ((area_id == null || area_id.equalsIgnoreCase(""))
				&& loggedInUser != null)
			area_id = loggedInUser.getArea_id();

		String fileName = "BILL-" + bill_id + ".pdf";
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Document document = new Document(PageSize.A4);

		try {
			ArrayList<MBillDTO> billList = getBill(this.bill_id,
					this.download_type);

			MeterService ms = new MeterService();
			ArrayList<CustomerApplianceDTO> applianceList = new ArrayList<CustomerApplianceDTO>();

			ReportFormat Event = new ReportFormat(getServletContext());
			PdfWriter writer = PdfWriter.getInstance(document, baos);
			// writer.setPageEvent(Event);
			PdfPCell pcell = null;

			document.open();
			String realPath = servlet
					.getRealPath("/resources/images/logo/JG.png"); // image path
			Image img = Image.getInstance(realPath);

			// img.scaleToFit(10f, 200f);
			// img.scalePercent(200f);
			img.scaleAbsolute(30f, 34f);
			// img.setAbsolutePosition(145f, 780f);
			img.setAbsolutePosition(150f, 767f); // rotate

			document.add(img);

			PdfPTable mTable = new PdfPTable(1);
			mTable.setWidthPercentage(100);
			pcell = new PdfPCell(new Paragraph(
					"JALALABAD GAS T & D SYSTEM LIMITED", ReportUtil.f11B));
			pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
			pcell.setBorder(Rectangle.NO_BORDER);
			mTable.addCell(pcell);

			pcell = new PdfPCell(new Paragraph("(A COMPANY OF PETROBANGLA)",
					ReportUtil.f8B));
			pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
			pcell.setBorder(Rectangle.NO_BORDER);
			mTable.addCell(pcell);

			pcell = new PdfPCell(new Paragraph(
					"HEAD OFFICE: MENDIBUGH, SYLHET", font3));
			pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
			pcell.setBorder(Rectangle.NO_BORDER);
			mTable.addCell(pcell);

			Chunk chunk1 = new Chunk("Regional Distribution: ", font2);
			Chunk chunk2 = new Chunk(String.valueOf(Area.values()[Integer
					.valueOf(getArea_id()) - 1]), ReportUtil.f8B);
			Paragraph p = new Paragraph();
			p.add(chunk1);
			p.add(chunk2);
			pcell = new PdfPCell(p);
			pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
			pcell.setBorder(Rectangle.NO_BORDER);
			mTable.addCell(pcell);

			document.add(mTable);

			PdfPTable subhTable = new PdfPTable(2);
			subhTable.setWidthPercentage(90);

			// ////////////////appliance table heads

			PdfPTable ApplianceTable = new PdfPTable(4);
			ApplianceTable.setWidthPercentage(90);

			PdfPTable cusTable = new PdfPTable(4);
			cusTable.setWidthPercentage(90);
			cusTable.setWidths(new int[] { 30, 100, 30, 30 });

			pcell = new PdfPCell(new Paragraph(" ", ReportUtil.f8B));
			pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
			pcell.setBorder(Rectangle.NO_BORDER);
			pcell.setPadding(10);
			pcell.setColspan(4);
			ApplianceTable.addCell(pcell);

			pcell = new PdfPCell(new Paragraph("Apliance Name", ReportUtil.f8B));
			pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
			pcell.setBorder(Rectangle.BOX);
			pcell.setPadding(5);
			ApplianceTable.addCell(pcell);

			pcell = new PdfPCell(new Paragraph("Apliance Quantity",
					ReportUtil.f8B));
			pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
			pcell.setBorder(Rectangle.BOX);
			pcell.setPadding(5);
			ApplianceTable.addCell(pcell);

			pcell = new PdfPCell(new Paragraph("Apliance Rate", ReportUtil.f8B));
			pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
			pcell.setBorder(Rectangle.BOX);
			pcell.setPadding(5);
			ApplianceTable.addCell(pcell);

			pcell = new PdfPCell(new Paragraph("Amount", ReportUtil.f8B));
			pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
			pcell.setBorder(Rectangle.BOX);
			pcell.setPadding(5);
			ApplianceTable.addCell(pcell);

			for (MBillDTO x : billList) {

				pcell = new PdfPCell(new Paragraph(" ", ReportUtil.f8B));
				pcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				pcell.setColspan(2);
				pcell.setPadding(10);
				pcell.setBorder(Rectangle.NO_BORDER);
				subhTable.addCell(pcell);

				pcell = new PdfPCell(new Paragraph("Date: "
						+ x.getBill_month_name() + ", " + x.getBill_year(),
						ReportUtil.f8B));
				pcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				pcell.setBorder(Rectangle.NO_BORDER);
				subhTable.addCell(pcell);

				pcell = new PdfPCell(new Paragraph(
						x.getCustomer_category_name(), ReportUtil.f8B));
				pcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				pcell.setBorder(Rectangle.NO_BORDER);
				subhTable.addCell(pcell);

				// //////////////////////////////////

				pcell = new PdfPCell(new Paragraph(" ", ReportUtil.f8));
				pcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				pcell.setBorder(Rectangle.NO_BORDER);
				pcell.setColspan(4);
				pcell.setPadding(15);
				cusTable.addCell(pcell);

				pcell = new PdfPCell(new Paragraph("ID", ReportUtil.f8));
				pcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				pcell.setPadding(5);
				pcell.setBorder(Rectangle.NO_BORDER);
				cusTable.addCell(pcell);

				pcell = new PdfPCell(new Paragraph(x.getCustomer_id(),
						ReportUtil.f8B));
				pcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				pcell.setBorder(Rectangle.NO_BORDER);
				pcell.setPadding(5);
				cusTable.addCell(pcell);

				pcell = new PdfPCell(new Paragraph("Issue Date", ReportUtil.f8));
				pcell.setHorizontalAlignment(Element.ALIGN_LEFT);			
				pcell.setBorder(Rectangle.BOX);
				cusTable.addCell(pcell);

				pcell = new PdfPCell(new Paragraph(x.getIssue_date(),
						ReportUtil.f8B));
				pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
				pcell.setBorder(Rectangle.BOX);				
				cusTable.addCell(pcell);

				pcell = new PdfPCell(new Paragraph("NAME", ReportUtil.f8));
				pcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				pcell.setBorder(Rectangle.NO_BORDER);
				pcell.setPadding(5);
				cusTable.addCell(pcell);

				pcell = new PdfPCell(new Paragraph(x.getCustomer_name(),
						ReportUtil.f8B));
				pcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				pcell.setBorder(Rectangle.NO_BORDER);
				pcell.setPadding(5);
				cusTable.addCell(pcell);

				pcell = new PdfPCell(new Paragraph("Last Date of Payment",
						ReportUtil.f8));
				pcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				pcell.setBorder(Rectangle.BOX);
				cusTable.addCell(pcell);

				pcell = new PdfPCell(new Paragraph(x.getLast_pay_date_w_sc(),
						ReportUtil.f8B));
				pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
				pcell.setBorder(Rectangle.BOX);				
				cusTable.addCell(pcell);

				pcell = new PdfPCell(new Paragraph("PROPITER NAME",
						ReportUtil.f8));
				pcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				pcell.setBorder(Rectangle.NO_BORDER);
				pcell.setPadding(5);
				cusTable.addCell(pcell);

				pcell = new PdfPCell(new Paragraph(x.getProprietor_name(),
						ReportUtil.f8B));
				pcell.setColspan(3);
				pcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				pcell.setBorder(Rectangle.NO_BORDER);
				pcell.setPadding(5);
				cusTable.addCell(pcell);

				pcell = new PdfPCell(new Paragraph("ADDRESS", ReportUtil.f8));
				pcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				pcell.setBorder(Rectangle.NO_BORDER);
				pcell.setPadding(5);
				cusTable.addCell(pcell);

				pcell = new PdfPCell(new Paragraph(x.getAddress(),
						ReportUtil.f8B));
				pcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				pcell.setColspan(3);
				pcell.setPadding(5);
				pcell.setBorder(Rectangle.NO_BORDER);				
				cusTable.addCell(pcell);

				applianceList = ms.getCustomerApplianceList(x.getCustomer_id());

				for (CustomerApplianceDTO a : applianceList) {

					pcell = new PdfPCell(new Paragraph(a.getApplianc_name(),
							ReportUtil.f8));
					pcell.setHorizontalAlignment(Element.ALIGN_LEFT);
					pcell.setBorder(Rectangle.BOX);
					pcell.setPadding(5);
					ApplianceTable.addCell(pcell);

					pcell = new PdfPCell(new Paragraph(a.getApplianc_qnt(),
							ReportUtil.f8));
					pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
					pcell.setBorder(Rectangle.BOX);
					pcell.setPadding(5);
					ApplianceTable.addCell(pcell);

					pcell = new PdfPCell(new Paragraph(a.getApplianc_rate(),
							ReportUtil.f8));
					pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
					pcell.setBorder(Rectangle.BOX);
					pcell.setPadding(5);
					ApplianceTable.addCell(pcell);

					double amount = Integer.parseInt(a.getApplianc_qnt())
							* Integer.parseInt(a.getApplianc_rate());

					pcell = new PdfPCell(new Paragraph(Double.toString(amount),
							ReportUtil.f8));
					pcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					pcell.setPaddingRight(30);
					pcell.setBorder(Rectangle.BOX);					
					ApplianceTable.addCell(pcell);

				}
				/*
				 * pcell = new PdfPCell(new Paragraph(x.getBilled_amount(),
				 * ReportUtil.f8));
				 * pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
				 * pcell.setBorder(Rectangle.BOX);
				 * pcell.setRowspan(applianceList.size());
				 * ApplianceTable.addCell(pcell);
				 */
				pcell = new PdfPCell(new Paragraph("Total Amount: ",
						ReportUtil.f8B));
				pcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				pcell.setBorder(Rectangle.BOX);
				pcell.setPadding(5);
				pcell.setColspan(3);
				ApplianceTable.addCell(pcell);

				pcell = new PdfPCell(new Paragraph(
						x.getBilled_amount() + ".00", ReportUtil.f8B));
				pcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				pcell.setBorder(Rectangle.BOX);
				pcell.setPaddingRight(30);
				ApplianceTable.addCell(pcell);

				pcell = new PdfPCell(new Paragraph(" ", ReportUtil.f8B));
				pcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				pcell.setColspan(4);
				pcell.setBorder(Rectangle.NO_BORDER);
				ApplianceTable.addCell(pcell);

				pcell = new PdfPCell(new Paragraph("IN WORD: "
						+ x.getAmount_in_word(), ReportUtil.f8B));
				pcell.setHorizontalAlignment(Element.ALIGN_LEFT);
				pcell.setBorder(Rectangle.NO_BORDER);
				pcell.setPadding(5);
				pcell.setColspan(4);
				ApplianceTable.addCell(pcell);

			}

			PdfPTable signTable = new PdfPTable(3);
			signTable.setWidthPercentage(100);

			pcell = new PdfPCell(new Paragraph(" ", font3));
			pcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			pcell.setColspan(3);
			pcell.setPadding(15);
			pcell.setBorder(Rectangle.NO_BORDER);
			signTable.addCell(pcell);

			pcell = new PdfPCell(new Paragraph("PREPARED BY-", font3));
			pcell.setHorizontalAlignment(Element.ALIGN_LEFT);
			pcell.setBorder(Rectangle.NO_BORDER);
			signTable.addCell(pcell);

			pcell = new PdfPCell(new Paragraph("CHECKED BY-", font3));
			pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
			pcell.setBorder(Rectangle.NO_BORDER);
			pcell.setPadding(5);
			signTable.addCell(pcell);

			pcell = new PdfPCell(new Paragraph("MANAGER/INCHARGE", font3));
			pcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			pcell.setBorder(Rectangle.NO_BORDER);
			pcell.setPadding(5);
			signTable.addCell(pcell);

			pcell = new PdfPCell(new Paragraph(" ", font3));
			pcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			pcell.setColspan(3);
			pcell.setBorder(Rectangle.NO_BORDER);
			signTable.addCell(pcell);

			document.add(subhTable);
			document.add(cusTable);
			document.add(ApplianceTable);
			document.add(signTable);
			document.close();
			ReportUtil rptUtil = new ReportUtil();
			rptUtil.downloadPdf(baos, getResponse(), fileName);
			document = null;

		} catch (Exception E) {
			E.printStackTrace();
		}
		return null;
	}

	public ServletContext getServlet() {
		return servlet;
	}

	public void setServlet(ServletContext servlet) {
		this.servlet = servlet;
	}

	public void setServletContext(ServletContext servlet) {
		this.servlet = servlet;
	}

	public String getBill_id() {
		return bill_id;
	}

	public void setBill_id(String bill_id) {
		this.bill_id = bill_id;
	}

	public String getCustomer_category() {
		return customer_category;
	}

	public void setCustomer_category(String customer_category) {
		this.customer_category = customer_category;
	}

	public String getArea_id() {
		return area_id;
	}

	public void setArea_id(String area_id) {
		this.area_id = area_id;
	}

	public String getCustomer_id() {
		return customer_id;
	}

	public void setCustomer_id(String customer_id) {
		this.customer_id = customer_id;
	}

	public String getCategory_name() {
		return category_name;
	}

	public void setCategory_name(String category_name) {
		this.category_name = category_name;
	}

	public String getBill_month() {
		return bill_month;
	}

	public void setBill_month(String bill_month) {
		this.bill_month = bill_month;
	}

	public String getBill_year() {
		return bill_year;
	}

	public void setBill_year(String bill_year) {
		this.bill_year = bill_year;
	}

	public String getDownload_type() {
		return download_type;
	}

	public void setDownload_type(String download_type) {
		this.download_type = download_type;
	}

	public String getBill_for() {
		return bill_for;
	}

	public void setBill_for(String bill_for) {
		this.bill_for = bill_for;
	}

	public int getSerial() {
		return serial;
	}

	public void setSerial(int serial) {
		this.serial = serial;
	}

	public static int getTextDiff() {
		return textDiff;
	}

	public static void setTextDiff(int textDiff) {
		BillNonMeteredCustomer.textDiff = textDiff;
	}

	public boolean isWater_mark() {
		return water_mark;
	}

	public void setWater_mark(boolean water_mark) {
		this.water_mark = water_mark;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
