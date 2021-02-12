package ma;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.io.Zip;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import io.github.bonigarcia.wdm.WebDriverManager;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

public class Barnstable {

	static WebDriver driver;
	static WebDriverWait wait;
	public static Properties prop;
	JavascriptExecutor js;

	static String Awsuser;
	static String Awspass;

	static String ResponseMessage;
	static InputStream InputData;

	static String SUFFIX = "/";
	static String S3folderName;
	static String bucket;

	static String Docs_File_path;
	static String ZipFolder;
	static String CurrentDt;

	static String OrderNo;
	static String Address1;
	static String Address2;
	static String Town;
	static String ZipCode;

	static String CompanyName = "";// "null";//"Genesis Title Agency LLC";
	static String BuyerName = "";// "Christy R. Burkhead, Brian A. Burkhead";//"null";//
	static String CoBuyerName = "";// "null";//"Todd2 Zelek2";

	static String ADD1 = "";
	static String comma = ",";
	static String LName = "";
	static String FName = "";
	static String Name = "";
	static String fullName = "";
	static String StreetNo = "";
	static String StreetName = "";
	
	@BeforeMethod
	public static void passArgsCMD()
	{
		try
		{
			prop=new Properties();
			FileInputStream ip=new FileInputStream("C:\\Users\\Vchavala\\eclipse-workspace\\ma\\src\\test\\java\\ma\\config.properties");
			prop.load(ip);
		}catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}catch(IOException e)
		{
			e.printStackTrace();
		}

		// Read propertyNames from command prompt using below maven command --
		//mvn clean install "-DOrderNo=197048" "-DAddress1=6 Huckleberry" "-DAddress2=null" "-DCity=Harwich" "-DState=MA" "-DCounty=Worcester" "-DZipCode=02645" "-DBuyerName=DOUGLAS MOHNS" "-DCoBuyerName=null" "-DCompanyName=null"
		//OrderNo="197048";ZipCode="02645";Address1="6 Huckleberry";BuyerName="DOUGLAS MOHNS";Town="Harwich";
		
				OrderNo = System.getProperty("OrderNo");
				System.out.println("OrderNo is: " + OrderNo);
				Address1 = System.getProperty("Address1");
				System.out.println("Address1 is : " + Address1);
				Address2 = System.getProperty("Address2");
				System.out.println("Address2 is : " + Address2);
				Town = System.getProperty("City");
				System.out.println("Town is : " + Town);
				String State = System.getProperty("State");
				System.out.println("State is : " + State);
				String County = System.getProperty("County");
				System.out.println("County is : " + County);
				ZipCode = System.getProperty("ZipCode");
				System.out.println("ZipCode is: " + ZipCode);

				BuyerName = System.getProperty("BuyerName");
				System.out.println("BuyerName is : " + BuyerName);
				CoBuyerName = System.getProperty("CoBuyerName");
				System.out.println("CoBuyerName is : " + CoBuyerName);
				CompanyName = System.getProperty("CompanyName");
				System.out.println("CompanyName is : " + CompanyName);

				// splits Address1 as ADD1, street no and street name and names
				Splitaddress();
				SplitNames();

				// prints current date and time
				CurrentDateTime();

				Docs_File_path = "C:\\Users\\Vchavala\\eclipse-workspace\\ma\\DownloadPDF\\"+ OrderNo + "_" + CurrentDt;
				ZipFolder = Docs_File_path + ".zip";

				// checks folder is available or not - if not available, creates folder with same name
				FolderExists();
				
				Awsuser=prop.getProperty("AWS_S3_UN");
				Awspass=prop.getProperty("AWS_S3_PW");
				bucket=prop.getProperty("AWS_S3_Bucket");
				S3folderName=prop.getProperty("S3folderName");				
									
	}

	@Test
	public static void BarnstableTest() throws Exception {
		
		//WebDriverManager.chromedriver().setup();
		System.setProperty("webdriver.chrome.driver","C:\\Users\\Vchavala\\eclipse-workspace\\ma\\Drivers\\chromedriver.exe");
		HashMap<String, Object> chromePref = new HashMap<String, Object>();
		chromePref.put("plugins.always_open_pdf_externally", true);
		chromePref.put("profile.default_content_settings.popup", 0);
		chromePref.put("download.default_directory", Docs_File_path);

		ChromeOptions options = new ChromeOptions();
		options.setExperimentalOption("prefs", chromePref);
		options.addArguments("--headless", "--window-size=3840,2160");

		driver = new ChromeDriver(options);
		//driver.manage().window().maximize();

		// Express VPN should connect//

		wait = new WebDriverWait(driver, 40);

		driver.get("https://publicrecords.netronline.com/state/MA/");

		driver.manage().timeouts().implicitlyWait(7, TimeUnit.SECONDS);

		try {
			CountyUrls();
			PACER();
			OFACdoc();
			zip_Folder();
			createBucket();

			Thread.sleep(3000);
			driver.quit();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			System.out.println("NETR Online: " + driver.getTitle());

			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("details-button"))).click();
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("proceed-link"))).click();

			CountyUrls();
			PACER();
			OFACdoc();
			zip_Folder();
			createBucket();// Connecting AWS S3 server and Upload document API

			Thread.sleep(3000);
			driver.quit();
		}
	}

	@AfterMethod
	public static void tearDown(ITestResult result) throws EmailException {
		
		if (ITestResult.FAILURE == result.getStatus()) {
			System.out.println("**Test is failed**");

			/*final String from = "vchavala@pippintechnologies.com";// "notifications@pippintechnologies.com";
			final String password = "vchavala@123";// "N0tifikSan@2019# ";
			String mailUsername = "vchavala@pippintitle.com";// "vchavala@pippintechnologies.com";
			String mailPassword = "vchavala@123";*/
			
			String from = prop.getProperty("from");
			String password = prop.getProperty("password");
			String mailUsername = prop.getProperty("mailUsername");
			String mailPassword = prop.getProperty("mailPassword");

			Email email = new SimpleEmail();
			System.out.println("Mail started");

			email.setHostName("smtp.googlemail.com");
			email.setSmtpPort(465);// 587
			email.setAuthenticator(new DefaultAuthenticator(mailUsername, mailPassword));
			email.setSSLOnConnect(true);
			email.setFrom("vchavala@pippintitle.com");
			email.setSubject("Automate MA_OrderNo: " + OrderNo + " is failed");
			email.setMsg("OrderNo: " + OrderNo + " is failed and please find the below details:" + "\n\n" + "ADD1 is: "
					+ ADD1 + "\n" + "ZipCode is: " + ZipCode + "\n" + "Town is: " + Town +"\n\n" + "Thanks & Regards "
					+ "\n" + "Venkateswarlu");
			
			//mail.SendEmail(ordernumber, "Hi ,"+"\n\n" + "Please Check this order number "+ ordernumber+" Order not found with Status " + statusGlobal + "\n\n Java Error:" + e + "\n\n Java Error Line Number: " + e.getStackTrace()[0].getLineNumber()+"\n\nLine number: "+ gbl_lineNumber + "\n\n Thanks & Regards " + "\n" + "Jeyanthi Priya");
			
			email.addTo("vchavala@pippintechnologies.com");
			email.send();

			System.out.println("Mail Sent");
			// IF ERROR OCCOUR- ENABLE TOGGLE IN
			// "https://myaccount.google.com/u/2/lesssecureapps?pli=1&pageId=none"

			driver.quit();
		}
	}

	public static void CountyUrls() throws InterruptedException {
		//ZipCode="01740";
		//String CountyDD="worcester_southern";
		
		try {
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("zip_code"))).sendKeys(ZipCode);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"zip_search\"]/input[2]")))
					.click();
			Thread.sleep(5000);

			String CountyDD = driver.findElement(By.xpath("//select[@id='county_sel']")).getAttribute("value");
			CountyDD=CountyDD.trim();

			System.out.println("County for this Order is: " + CountyDD);

			// storing County names
			String County_barnstable = "barnstable";
			String County_berkshire_southern = "berkshire_southern";
			String County_berkshire_northern = "berkshire_northern";
			String County_berkshire_middle = "berkshire_middle";
			String County_bristol_fall_river = "bristol_fall_river";
			String County_bristol_northern = "bristol_northern";
			String County_bristol_southern = "bristol_southern";
			String County_dukes = "dukes";
			String County_essex_northern = "essex_northern";
			String County_essex_southern = "essex_southern";
			String County_franklin = "franklin";
			String County_hampden = "hampden";
			String County_hampshire = "hampshire";
			String County_middlesex_northern = "middlesex_northern";
			String County_middlesex_southern = "middlesex_southern";
			String County_nantucket = "nantucket";
			String County_norfolk = "norfolk";
			String County_plymouth = "plymouth";
			String County_suffolk = "suffolk";
			String County_worcester_northern = "worcester_northern";
			String County_worcester_southern = "worcester_southern";

			// All county URLS
			// Comparing County from NETR Online with County names
			// If county name is matched, *If loop* will run for perticular county name

			// barnstable
			if (CountyDD.equalsIgnoreCase(County_barnstable)) {
				driver.get("https://search.barnstabledeeds.org/ALIS/WW400R.HTM?WSIQTP=LR01D&WSKYCD=N");
				Thread.sleep(3000);

				try {
					BarnstableSearchdetails();
					try {
						System.out.println("Town for this Order is: " + Town);
						Barnstable_Town_URLs();
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("Please check the site is available or " + e.getMessage());
						System.out.println(Town + ": " + driver.getTitle());
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Please check the site is available or " + e.getMessage());
					System.out.println(CountyDD + ": " + driver.getTitle());
				}

			}

			// berkshire_southern
			else if (CountyDD.equalsIgnoreCase(County_berkshire_southern)) {
				driver.get("https://www.masslandrecords.com/BerkSouth/");
				// driver.get("https://www.masslandrecords.com/BerkNorth/");
				Thread.sleep(5000);

				try {
					Berkshireprocess();
					try {
						System.out.println("Town for this Order is: " + Town);
						Berkshire_Town_URLs();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println("Please check the site is available or " + e.getMessage());
						System.out.println(Town + ": " + driver.getTitle());
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Please check the site is available or " + e.getMessage());
					System.out.println(Town + ": " + driver.getTitle());
				}

			}

			// berkshire_northern
			else if (CountyDD.equalsIgnoreCase(County_berkshire_northern)) {
				driver.get("https://www.masslandrecords.com/BerkNorth/");
				Thread.sleep(5000);
				try {
					Berkshireprocess();
					try {
						System.out.println("Town for this Order is: " + Town);
						Berkshire_Town_URLs();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println("Please check the site is available or " + e.getMessage());
						System.out.println(Town + ": " + driver.getTitle());
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Please check the site is available or " + e.getMessage());
					System.out.println(Town + ": " + driver.getTitle());
				}
			}

			// berkshire_middle
			else if (CountyDD.equalsIgnoreCase(County_berkshire_middle)) {
				driver.get("https://www.masslandrecords.com/BerkMiddle/");
				Thread.sleep(5000);
				try {
					Berkshireprocess();
					try {
						System.out.println("Town for this Order is: " + Town);
						Berkshire_Town_URLs();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println("Please check the site is available or " + e.getMessage());
						System.out.println(Town + ": " + driver.getTitle());
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Please check the site is available or " + e.getMessage());
					System.out.println(Town + ": " + driver.getTitle());
				}
			}

			// bristol_fall_river
			else if (CountyDD.equalsIgnoreCase(County_bristol_fall_river)) {
				driver.get("https://www.fallriverdeeds.com/D/Default.aspx");
				Thread.sleep(3000);

				try {
					System.out.println("Town for this Order is: " + Town);
					Bristol_Town_URLs();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("Please check the site is available or " + e.getMessage());
					System.out.println(Town + ": " + driver.getTitle());
				}

			}

			// bristol_southern
			else if (CountyDD.equalsIgnoreCase(County_bristol_southern)) {
				driver.get(
						"https://i2e.uslandrecords.com/MA/BristolSouth/D/Default.aspx?AspxAutoDetectCookieSupport=1");
				Thread.sleep(3000);
				try {
					System.out.println("Town for this Order is: " + Town);
					Bristol_Town_URLs();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("Please check the site is available or " + e.getMessage());
					System.out.println(Town + ": " + driver.getTitle());
				}
			}

			// bristol_southern
			else if (CountyDD.equalsIgnoreCase(County_bristol_northern)) {
				// bristol_northern
				driver.get("http://www.tauntondeeds.com/Searches/RecordedLand/RecordedLandInquiry.aspx"); // worcester_northern
				Thread.sleep(5000);
				try {
					System.out.println("Town for this Order is: " + Town);
					Bristol_Town_URLs();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("Please check the site is available or " + e.getMessage());
					System.out.println(Town + ": " + driver.getTitle());
				}
			}

			// dukes
			else if (CountyDD.equalsIgnoreCase(County_dukes)) {
				driver.get("https://www.masslandrecords.com/Dukes/");
				Thread.sleep(5000);
				try {
					Berkshireprocess();
					try {
						System.out.println("Town for this Order is: " + Town);
						Dukes_Town_URLs();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println("Please check the site is available or " + e.getMessage());
						System.out.println(Town + ": " + driver.getTitle());
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Please check the site is available or " + e.getMessage());
					System.out.println(Town + ": " + driver.getTitle());
				}
			}

			// essex_northern
			else if (CountyDD.equalsIgnoreCase(County_essex_northern)) {
				driver.get("http://search.lawrencedeeds.com/ALIS/WW400R.HTM?WSIQTP=LR01D&WSKYCD=N");
				Thread.sleep(5000);

				try {
					BarnstableSearchdetails();
					try {
						System.out.println("Town for this Order is: " + Town);
						Essex_Town_URLs();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println("Please check the site is available or " + e.getMessage());
						System.out.println(Town + ": " + driver.getTitle());
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Please check the site is available or " + e.getMessage());
					System.out.println(Town + ": " + driver.getTitle());
				}
			}

			// essex_southern
			else if (CountyDD.equalsIgnoreCase(County_essex_southern)) {
				driver.get("https://salemdeeds.com/salemdeeds/Default2.aspx");
				Thread.sleep(3000);
				driver.get("https://salemdeeds.com/salemdeeds/Defaultsearch2.aspx");
				Thread.sleep(3000);
				try {
					Essex_process();
					try {
						System.out.println("Town for this Order is: " + Town);
						Essex_Town_URLs();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println("Please check the site is available or " + e.getMessage());
						System.out.println(Town + ": " + driver.getTitle());
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("Please check the site is available or " + e.getMessage());
					System.out.println(Town + ": " + driver.getTitle());
				}

			}

			// franklin
			else if (CountyDD.equalsIgnoreCase(County_franklin)) {
				driver.get("https://www.masslandrecords.com/Franklin/");
				Thread.sleep(5000);
				try {
					franklinprocess();
					try {
						System.out.println("Town for this Order is: " + Town);
						Franklin_Town_URLs();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println("Please check the site is available or " + e.getMessage());
						System.out.println(Town + ": " + driver.getTitle());
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Please check the site is available or " + e.getMessage());
					System.out.println(Town + ": " + driver.getTitle());
				}
			}

			// hampden
			else if (CountyDD.equalsIgnoreCase(County_hampden)) {
				driver.get("https://search.hampdendeeds.com/html/Hampden/V3/search.html");
			}

			// hampshire
			else if (CountyDD.equalsIgnoreCase(County_hampshire)) {
				driver.get("https://www.masslandrecords.com/Hampshire/");
				Thread.sleep(5000);
				try {
					Berkshireprocess();
					try {
						System.out.println("Town for this Order is: " + Town);
						Hampshire_Town_URLs();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println("Please check the site is available or " + e.getMessage());
						System.out.println(Town + ": " + driver.getTitle());
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Please check the site is available or " + e.getMessage());
					System.out.println(Town + ": " + driver.getTitle());
				}
			}

			// middlesex_northern
			else if (CountyDD.equalsIgnoreCase(County_middlesex_northern)) {
				driver.get("https://www.masslandrecords.com/MiddlesexNorth/");
				Thread.sleep(5000);
				try {
					Berkshireprocess();
					try {
						System.out.println("Town for this Order is: " + Town);
						MiddlesexTest_Town_URLs();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println("Please check the site is available or " + e.getMessage());
						System.out.println(Town + ": " + driver.getTitle());
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Please check the site is available or " + e.getMessage());
					System.out.println(Town + ": " + driver.getTitle());
				}
			}

			// middlesex_southern
			else if (CountyDD.equalsIgnoreCase(County_middlesex_southern)) {
				driver.get("https://www.masslandrecords.com/MiddlesexSouth/");
				Thread.sleep(5000);
				try {
					Berkshireprocess();
					try {
						System.out.println("Town for this Order is: " + Town);
						MiddlesexTest_Town_URLs();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println("Please check the site is available or " + e.getMessage());
						System.out.println(Town + ": " + driver.getTitle());
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Please check the site is available or " + e.getMessage());
					System.out.println(Town + ": " + driver.getTitle());
				}
			}

			// nantucket
			else if (CountyDD.equalsIgnoreCase(County_nantucket)) {
				driver.get("https://www.masslandrecords.com/Nantucket/");
				Thread.sleep(5000);
				try {
					Berkshireprocess();
					try {
						System.out.println("Town for this Order is: " + Town);
						GIS_Nantucket_Town_URLs();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println("Please check the site is available or " + e.getMessage());
						System.out.println(Town + ": " + driver.getTitle());
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Please check the site is available or " + e.getMessage());
					System.out.println(Town + ": " + driver.getTitle());
				}
			}

			// norfolk
			else if (CountyDD.equalsIgnoreCase(County_norfolk)) {
				driver.get("http://www.norfolkresearch.org/ALIS/WW400R.HTM?WSIQTP=LR01D&WSKYCD=N");
				Thread.sleep(5000);

				try {
					BarnstableSearchdetails();
					try {
						System.out.println("Town for this Order is: " + Town);
						Norfolk_Town_URLs();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println("Please check the site is available or " + e.getMessage());
						System.out.println(Town + ": " + driver.getTitle());
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Please check the site is available or " + e.getMessage());
					System.out.println(Town + ": " + driver.getTitle());
				}
			}

			// plymouth
			else if (CountyDD.equalsIgnoreCase(County_plymouth)) {
				//driver.get("http://titleview.org/plymouthdeeds/");
				//Thread.sleep(5000);
					try {
						System.out.println("Town for this Order is: " + Town);
						Plymouth_Town_URLs();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println("Please check the site is available or " + e.getMessage());
						System.out.println(Town + ": " + driver.getTitle());
					}
			}

			// suffolk
			else if (CountyDD.equalsIgnoreCase(County_suffolk)) {
				driver.get("https://www.masslandrecords.com/Suffolk/");
				Thread.sleep(5000);
				try {
					Berkshireprocess();
					try {
						System.out.println("Town for this Order is: " + Town);
						Suffolk_Town_URLs();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println("Please check the site is available or " + e.getMessage());
						System.out.println(Town + ": " + driver.getTitle());
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Please check the site is available or " + e.getMessage());
					System.out.println(Town + ": " + driver.getTitle());
				}
			}

			// worcester_northern
			else if (CountyDD.equalsIgnoreCase(County_worcester_northern)) {

				driver.get("http://www.fitchburgdeeds.com/ALIS/WW400R.HTM?WSIQTP=LR01D&WSKYCD=N");
				Thread.sleep(5000);

				try {
					BarnstableSearchdetails();
					try {
						System.out.println("Town for this Order is: " + Town);
						Worcester_Town_URLs();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println("Please check the site is available or " + e.getMessage());
						System.out.println(Town + ": " + driver.getTitle());
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Please check the site is available or " + e.getMessage());
					System.out.println(Town + ": " + driver.getTitle());
				}
			}

			// worcester_southern
			else if (CountyDD.equalsIgnoreCase(County_worcester_southern)) {
								
				driver.get("https://www.masslandrecords.com/Worcester/default.aspx?AspxAutoDetectCookieSupport=1");
				Thread.sleep(5000);
				try {
					franklinprocess();
					try {
						System.out.println("Town for this Order is: " + Town);
						Worcester_Town_URLs();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println("Please check the site is available or " + e.getMessage());
						System.out.println(Town + ": " + driver.getTitle());
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Please check the site is available or " + e.getMessage());
					System.out.println(Town + ": " + driver.getTitle());
				}
			}

			// no county or ZIP is provided
			else {
				System.out.println("Please enter correct ZIP code or County");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			System.out.println("Please enter correct ZIP code or " + e.getMessage());
			Thread.sleep(3000);
		}
	}

	// PACER Site
	public static void PACER() throws InterruptedException {
		// PACER Credentials
		/*String pacerUserName = "Vikas.Pippin";
		String pacerPassword = "Title@456";*/
		
		//LName="ANGELLO";
		
		String pacerUserName=prop.getProperty("PacerUN");
		String pacerPassword =prop.getProperty("PacerPW");

		driver.get(prop.getProperty("PacerUrl"));
		Thread.sleep(5000);

		try {
			// Enter User Name
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("loginForm:loginName"))).sendKeys(pacerUserName);
			// Enter Password
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("loginForm:password"))).sendKeys(pacerPassword);
				// Click on Login button
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("loginForm:fbtnLogin"))).click();
				Thread.sleep(3000);

				try {					
						PACERimages();

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println(e.getMessage());
					Thread.sleep(3000);
					// Click on findPartiesAdvanced link
					wait.until(ExpectedConditions.presenceOfElementLocated(By.id("frmSearch:findPartiesAdvanced"))).click();
					Thread.sleep(3000);
					try {
						PACERimages();
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
			System.out.println("PACER: " + driver.getTitle());
		}

	}

	public static void PACERimages() throws InterruptedException {
		String PState = "Massachusetts";
		/*
		 * String LName="CHARLTON"; String FName="KEVINC";
		 */

		// Enter Last Name or Entity Name
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='frmSearch:txtPartyNameLast']")))
				.sendKeys(LName);
		// Enter First Name
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='frmSearch:txtPartyNameFirst']")))
				.sendKeys(FName);
		Thread.sleep(3000);

		//click on Region
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='frmSearch:scmRegion']/ul"))).click();
		Thread.sleep(5000);
		// Enter State
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id=\"frmSearch:scmRegion_panel\"]/div[1]/div[2]/input")))
				.sendKeys(PState);

		// click on checkbox
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='frmSearch:scmRegion_panel']/div[1]/div[1]/div[2]/span")))
				.click();
		
			// Click on Search button
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("frmSearch:btnSearch"))).click();
			Thread.sleep(3000);
			
			String PACER_title="Advanced Party Search | PACER: PACER Case Locator";
			String getPACER_title=driver.getTitle();
			System.out.println("***Pacer title is: "+getPACER_title);
			
			if(PACER_title.equals(getPACER_title))
			{	
				driver.navigate().back();
			}
			else {
					for (int CN = 0; CN < driver.findElements(By.xpath("//*[@id='frmSearch']//a[@class='ui-link ui-widget psc-command-link']")).size(); CN++) {
					try {
					// Click on case number link
					driver.findElements(
							By.xpath("//*[@id='frmSearch']//a[@class='ui-link ui-widget psc-command-link']")).get(CN)
							.click();
					Thread.sleep(2000);
					System.out.println("Document No: " + CN);
					Thread.sleep(5000);

					// Window handling
					ArrayList<String> tab = new ArrayList<String>(driver.getWindowHandles());
					driver.switchTo().window(tab.get(1));
					Thread.sleep(5000);

					try {
						// Click on case Summary link 
						wait.until(ExpectedConditions.presenceOfElementLocated(
								By.xpath("//*[@id='cmecfMainContent']/table/tbody/tr/td//*[text()='Case Summary']"))).click();
						Thread.sleep(3000);

						String CSpath = Docs_File_path + "/PACER_CaseSummary" + CN + ".png";
						Screenshot fpScreenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000))
								.takeScreenshot(driver);
						ImageIO.write(fpScreenshot.getImage(), "PNG", new File(CSpath));
						Thread.sleep(3000);

						driver.navigate().back();
						Thread.sleep(3000);

						try {
							// Click on Docket report link
							wait.until(ExpectedConditions.presenceOfElementLocated(
									By.xpath("//*[@id='cmecfMainContent']/table/tbody/tr/td//*[text()='Docket Report ...']"))).click();
							// Click on Run report button
							wait.until(ExpectedConditions.presenceOfElementLocated(By.name("button1"))).click();
							Thread.sleep(3000);

							String RLpath = Docs_File_path + "/PACER_ReportLink" + CN + ".png";
							Screenshot fpScreenshotRL = new AShot()
									.shootingStrategy(ShootingStrategies.viewportPasting(1000)).takeScreenshot(driver);
							ImageIO.write(fpScreenshotRL.getImage(), "PNG", new File(RLpath));
							Thread.sleep(3000);

							driver.navigate().back();
							Thread.sleep(3000);

							driver.navigate().back();
							Thread.sleep(3000);

							try {
								// Click on Status link
								wait.until(ExpectedConditions.presenceOfElementLocated(
										By.xpath("//*[@id='cmecfMainContent']/table/tbody/tr/td//*[text()='Status']"))).click();
								Thread.sleep(3000);

								String SLpath = Docs_File_path + "/PACER_StatusLink" + CN + ".png";
								Screenshot fpScreenshotSL = new AShot()
										.shootingStrategy(ShootingStrategies.viewportPasting(1000))
										.takeScreenshot(driver);
								ImageIO.write(fpScreenshotSL.getImage(), "PNG", new File(SLpath));
								Thread.sleep(3000);

								driver.navigate().back();
								Thread.sleep(3000);

								driver.close();
								Thread.sleep(3000);
								driver.switchTo().window(tab.get(0));
								Thread.sleep(3000);

								Thread.sleep(3000);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								System.out.println(e.getMessage());
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							System.out.println(e.getMessage());
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println(e.getMessage());
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("Documents are not available in PACER" + e.getMessage());
					
					driver.navigate().back();				
				}
			}
			driver.navigate().back();
		}
	}

	// OFAC site
	public static void OFACdoc() throws InterruptedException, IOException {
		String Name = LName + " " + FName;
		driver.get("https://sanctionssearch.ofac.treas.gov/");
		Thread.sleep(5000);

		try {
			// Enter Name
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_txtLastName")))
					.sendKeys(Name);

			try {
				// Click on Click to Search using criteria below button
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_btnSearch"))).click();
				Thread.sleep(3000);
				
					// multiple document links are available
					System.out.println("Documents available in OFAC: "
							+ driver.findElements(By.xpath("//a[@id='btnDetails']")).size());
					for (int OF = 0; OF < driver.findElements(By.xpath("//a[@id='btnDetails']")).size(); OF++) {
						try {
						// CLick on result
						driver.findElements(By.xpath("//a[@id='btnDetails']")).get(OF).click();
						System.out.println("Document is available: " + OF);

						// add Ashot jar file to Java project - for maven, add Ashot dependency
						String path = Docs_File_path + "/OFAC_" + Name + OF + ".png";
						Screenshot fpScreenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000))
								.takeScreenshot(driver);
						ImageIO.write(fpScreenshot.getImage(), "PNG", new File(path));
						Thread.sleep(3000);
						wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_btnBack"))).click();
						Thread.sleep(3000);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println("Documents are not available in OFAC" + e.getMessage());
					}

					}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
			System.out.println("OFAC: " + driver.getTitle());
		}

	}

	// Convert folder to ZIP folder
	public static void zip_Folder() throws IOException {
		int fileCount = 0;
		File file = new File(Docs_File_path);
		// Folder contains PDF files
		if (file.exists()) {
			System.out.println("exists");
			fileCount = file.list().length;
			System.out.println("File Count is: " + fileCount);
		} else {
			System.out.println("Not exists");
		}

		if (fileCount != 0) {
			String zip_File_path = Zip.zip(new File(Docs_File_path));
			System.out.println("zip_File_path" + zip_File_path);

			// Converting to Zip folder
			BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(ZipFolder), 1000);
			System.out.println("stream" + stream);
			byte[] decode = Base64.getDecoder().decode(zip_File_path);
			stream.write(decode);
			stream.close();
			System.out.println("**Converted ZIP folder**");
			System.out.println("**ZIP folder path is : " + ZipFolder);

			// Delete folder
			FileUtils.deleteDirectory(file);
			System.out.println("**Folder deleted :"+Docs_File_path);
		} else {
			// Delete folder
			FileUtils.deleteDirectory(file);
			System.out.println("**Folder deleted :"+Docs_File_path);
		}
	}

	// checks folder is available or not - if not available creates folder with same name
	public static void FolderExists() {
		File f = new File(Docs_File_path);
		if (f.exists() && f.isDirectory()) {
			System.out.println("folder is Exists: " + Docs_File_path);
			// if the file is present then it will show the msg
		} else {
			System.out.println("NOT Exists");
			// if the file is Not present then it will show the msg
			f.mkdir();
			System.out.print("Folder created: " + Docs_File_path);
		}

	}

	// Barnstable County - Name and Address search documents
	public static void BarnstableSearchdetails() throws InterruptedException {
		try {
			// Enter Last Name
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("W9SNM"))).sendKeys(LName);

			// Enter First Name
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("W9GNM"))).sendKeys(FName);

			System.out.println("**Name Search records**");

			BarnstableDropDown();

			// click on Address (partial) tab
			wait.until(ExpectedConditions.presenceOfElementLocated(
					By.xpath("/html/body/div/div[2]/div/div[4]/div[3]/div/div/div/ul/li[2]/a"))).click();
			System.out.println("Address is : " + ADD1);

			// Enter address
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("W9PADR"))).sendKeys(ADD1);

			System.out.println("**Address Search records**");

			BarnstableDropDown();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Records are not able to get for Barnstable");
		}

	}

	public static void BarnstableDropDown() throws InterruptedException {
		String barnstableDDValues = "M,*DD,*LN,FJ/TX";
		String barnstableDD[] = barnstableDDValues.split(",");

		for (int Barn_DD = 0; Barn_DD < barnstableDD.length; Barn_DD++) {
			System.out.println("barnstable DD : " + barnstableDD[Barn_DD]);
			String DDvalue = barnstableDD[Barn_DD];

			// Identify Document Type dropdown and values *LN , M , *DD
			WebElement DocTypeDD;
			DocTypeDD = driver.findElement(By.id("W9ABR"));
			Select s = new Select(DocTypeDD);
			// String DD = null;

			// Selecting Deed from Dropdow
			s.selectByValue(DDvalue);
			Thread.sleep(3000);

			BarnstableTownDDValues();
			Thread.sleep(3000);

			// Click on Search button
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@type='submit']"))).click();
			Thread.sleep(3000);

			barnstable_viewicon();
		}
	}

	public static void BarnstableTownDDValues() {
		String Barn_Town_Falmouth = "Falmouth";
		String Barn_Town_Bourne = "Bourne";
		String Barn_Town_Brewster = "Brewster";
		String Barn_Town_Dennis = "Dennis";
		String Barn_Town_Harwich = "Harwich";
		String Barn_Town_Mashpee = "Mashpee";
		String Barn_Town_Orleans = "Orleans";
		String Barn_Town_Sandwich = "Sandwich";
		String Barn_Town_Truro = "Truro";
		String Barn_Town_Wellfleet = "Wellfleet";
		String Barn_Town_Barnstable = "Barnstable";
		String Barn_Town_Yarmouth = "Yarmouth";
		String Barn_Town_Chatham = "Chatham";
		String Barn_Town_Eastham = "Eastham";
		String Barn_Town_Provincetown = "Provincetown";

		WebElement BarnTown;
		BarnTown = driver.findElement(By.id("W9TOWN"));
		Select s = new Select(BarnTown);

		if (Town.equalsIgnoreCase(Barn_Town_Falmouth)) {
			s.selectByVisibleText(Town);
		} else if (Town.equalsIgnoreCase(Barn_Town_Bourne)) {
			s.selectByVisibleText(Town);
		} else if (Town.equalsIgnoreCase(Barn_Town_Brewster)) {
			s.selectByVisibleText(Town);
		} else if (Town.equalsIgnoreCase(Barn_Town_Dennis)) {
			s.selectByVisibleText(Town);
		} else if (Town.equalsIgnoreCase(Barn_Town_Harwich)) {
			s.selectByVisibleText(Town);
		} else if (Town.equalsIgnoreCase(Barn_Town_Mashpee)) {
			s.selectByVisibleText(Town);
		} else if (Town.equalsIgnoreCase(Barn_Town_Orleans)) {
			s.selectByVisibleText(Town);
		} else if (Town.equalsIgnoreCase(Barn_Town_Sandwich)) {
			s.selectByVisibleText(Town);
		} else if (Town.equalsIgnoreCase(Barn_Town_Truro)) {
			s.selectByVisibleText(Town);
		} else if (Town.equalsIgnoreCase(Barn_Town_Wellfleet)) {
			s.selectByVisibleText(Town);
		} else if (Town.equalsIgnoreCase(Barn_Town_Barnstable)) {
			s.selectByVisibleText(Town);
		} else if (Town.equalsIgnoreCase(Barn_Town_Yarmouth)) {
			s.selectByVisibleText(Town);
		} else if (Town.equalsIgnoreCase(Barn_Town_Chatham)) {
			s.selectByVisibleText(Town);
		} else if (Town.equalsIgnoreCase(Barn_Town_Eastham)) {
			s.selectByVisibleText(Town);
		} else if (Town.equalsIgnoreCase(Barn_Town_Provincetown)) {
			s.selectByVisibleText(Town);
		} else {
			s.selectByValue("*ALL");
		}

	}

	public static void barnstable_viewicon() throws InterruptedException {
		System.out.println(
				"No. of documents: " + driver.findElements(By.xpath("//*[@title=\"View Document Image\"]")).size());
		for (int Barn_vi = 0; Barn_vi < driver.findElements(By.xpath("//*[@title=\"View Document Image\"]"))
				.size(); Barn_vi++) {
			System.out.println("Document No:" + Barn_vi);
			Thread.sleep(3000);

			try {
				driver.findElements(By.xpath("//*[@title=\"View Document Image\"]")).get(Barn_vi).click();
				Thread.sleep(3000);

				// Window handling
				ArrayList<String> tab = new ArrayList<String>(driver.getWindowHandles());
				driver.switchTo().window(tab.get(1));
				Thread.sleep(5000);

				// Click on View all images
				// WebElement ViewImage= driver.findElement(By.linkText("View the Image"));
				wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("View the Image"))).click();
				driver.close();

				driver.switchTo().window(tab.get(0));
				Thread.sleep(5000);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("No Records are  available");
			}

		}
		// System.out.println("Records are available in folder");
		driver.navigate().back();
		Thread.sleep(5000);

	}

	// Barnstable Township urls
	public static void Barnstable_Town_URLs() throws InterruptedException, IOException {
		String Barn_Town_Falmouth = "Falmouth";
		String Barn_Town_Bourne = "Bourne";
		String Barn_Town_Brewster = "Brewster";
		String Barn_Town_Dennis = "Dennis";
		String Barn_Town_Harwich = "Harwich";
		String Barn_Town_Mashpee = "Mashpee";
		String Barn_Town_Orleans = "Orleans";
		String Barn_Town_Sandwich = "Sandwich";
		String Barn_Town_Truro = "Truro";
		String Barn_Town_Wellfleet = "Wellfleet";
		String Barn_Town_Barnstable = "Barnstable";
		String Barn_Town_Yarmouth = "Yarmouth";
		String Barn_Town_Chatham = "Chatham";
		String Barn_Town_Eastham = "Eastham";
		String Barn_Town_Provincetown = "Provincetown";

		if (Town.equalsIgnoreCase(Barn_Town_Falmouth)) {
			driver.get("http://falmouth.patriotproperties.com/default.asp?br=exp&vr=6");
			Thread.sleep(3000);
			try {
				Falmouth_Township();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Barn_Town_Bourne)) {
			driver.get("http://www.assessedvalues2.com/index.aspx?jurcode=36");
			Thread.sleep(3000);
			try {
				AssessorHarwich();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Barn_Town_Brewster)) {
			driver.get("http://www.assessedvalues2.com/index.aspx?jurcode=41");
			Thread.sleep(3000);
			try {
				AssessorHarwich();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Barn_Town_Dennis)) {
			driver.get("http://www.assessedvalues2.com/index.aspx?jurcode=75");
			Thread.sleep(3000);
			try {
				AssessorHarwich();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Barn_Town_Harwich)) {
			driver.get("http://www.assessedvalues2.com/Index.aspx?jurcode=126");
			Thread.sleep(3000);
			try {
				AssessorHarwich();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Barn_Town_Mashpee)) {
			driver.get("http://www.assessedvalues2.com/index.aspx?jurcode=172");
			Thread.sleep(3000);
			try {
				AssessorHarwich();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Barn_Town_Orleans)) {
			driver.get("http://www.assessedvalues2.com/index.aspx?jurcode=224");
			Thread.sleep(3000);
			try {
				AssessorHarwich();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Barn_Town_Sandwich)) {
			driver.get("http://www.assessedvalues2.com/index.aspx?jurcode=261");
			Thread.sleep(3000);
			try {
				AssessorHarwich();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Barn_Town_Truro)) {
			driver.get("http://www.assessedvalues2.com/index.aspx?jurcode=300");
			Thread.sleep(3000);
			try {
				AssessorHarwich();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Barn_Town_Wellfleet)) {

			driver.get("http://www.assessedvalues2.com/index.aspx?jurcode=318");
			Thread.sleep(3000);
			try {
				AssessorHarwich();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Barn_Town_Barnstable)) {

			driver.get("http://gis.vgsi.com/Barnstablema/");
			Thread.sleep(3000);
			try {
				barnstable_Township();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Barn_Town_Yarmouth)) {

			driver.get("http://gis.vgsi.com/Yarmouthma/");
			Thread.sleep(3000);
			try {
				barnstable_Township();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Barn_Town_Chatham)) {
			driver.get(
					"https://www.mapsonline.net/chathamma/web_assessor/search.php#sid=7578f4cda38af688d69268b50bdbeb46	");
			Thread.sleep(3000);
			try {
				CHATHAMandPROVINCE_Township();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Barn_Town_Eastham)) {
			driver.get("http://mapsonline.net/easthamma/web_assessor/search.php#sid=7578f4cda38af688d69268b50bdbeb46");
			Thread.sleep(3000);
			try {
				Eastham_Township();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Barn_Town_Provincetown)) {
			driver.get(
					"http://www.mapsonline.net/provincetownma/web_assessor/search.php#sid=890a22a0d75d07417f61322770444bcf");
			Thread.sleep(3000);
			try {
				CHATHAMandPROVINCE_Township();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

	}

	public static void Falmouth_Township() throws InterruptedException, IOException, AWTException {
		driver.switchTo().frame("middle");
    	driver.findElement(By.xpath("//input[@name='SearchStreetName']")).sendKeys("McMahon");
    
    	driver.findElement(By.xpath("//input[@name='SearchStreetNumber']")).sendKeys("1");
    	
    	driver.findElement(By.xpath("//input[@name='cmdGo']")).click();
    	Thread.sleep(4000);
    	
    	Robot r =  new Robot();
		
		r.keyPress(KeyEvent.VK_TAB);
		r.keyRelease(KeyEvent.VK_TAB);
		
		r.keyPress(KeyEvent.VK_TAB);
		r.keyRelease(KeyEvent.VK_TAB);
		
		r.keyPress(KeyEvent.VK_TAB);
		r.keyRelease(KeyEvent.VK_TAB);
		
		r.keyPress(KeyEvent.VK_TAB);
		r.keyRelease(KeyEvent.VK_TAB);
		
		r.keyPress(KeyEvent.VK_TAB);
		r.keyRelease(KeyEvent.VK_TAB);
		
		r.keyPress(KeyEvent.VK_TAB);
		r.keyRelease(KeyEvent.VK_TAB);
		
		r.keyPress(KeyEvent.VK_TAB);
		r.keyRelease(KeyEvent.VK_TAB);
		
		r.keyPress(KeyEvent.VK_TAB);
		r.keyRelease(KeyEvent.VK_TAB);
		
		r.keyPress(KeyEvent.VK_TAB);
		r.keyRelease(KeyEvent.VK_TAB);
		
		r.keyPress(KeyEvent.VK_TAB);
		r.keyRelease(KeyEvent.VK_TAB);
		
		r.keyPress(KeyEvent.VK_TAB);
		r.keyRelease(KeyEvent.VK_TAB);
		
		r.keyPress(KeyEvent.VK_TAB);
		r.keyRelease(KeyEvent.VK_TAB);
		
		r.keyPress(KeyEvent.VK_TAB);
		r.keyRelease(KeyEvent.VK_TAB);
		
		r.keyPress(KeyEvent.VK_TAB);
		r.keyRelease(KeyEvent.VK_TAB);
		
		r.keyPress(KeyEvent.VK_TAB);
		r.keyRelease(KeyEvent.VK_TAB);
		
		r.keyPress(KeyEvent.VK_TAB);
		r.keyRelease(KeyEvent.VK_TAB);
		
		r.keyPress(KeyEvent.VK_TAB);
		r.keyRelease(KeyEvent.VK_TAB);
		
		r.keyPress(KeyEvent.VK_TAB);
		r.keyRelease(KeyEvent.VK_TAB);
		
		r.keyPress(KeyEvent.VK_ENTER);
		r.keyRelease(KeyEvent.VK_ENTER);
		Thread.sleep(4000);
		
    	/*WebElement framebottom=driver.findElement(By.xpath("//frame[@src=\"home-bottom.asp\"]"));
    	driver.switchTo().frame(framebottom);
    	Thread.sleep(4000);*/
    	/*try {
			driver.findElement(By.xpath("//*[@id='T1']/tbody/tr[1]/td[1]/a")).click();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			driver.findElement(By.xpath("(//*[text()='Print page 1 of 1'])[2]")).click();
			Thread.sleep(4000);
		}*/
    	driver.switchTo().frame("middle");
    	Thread.sleep(4000);
    	driver.findElement(By.xpath("//*[text()='Printable Record Card']")).click();
    	
    	// Window handling
		ArrayList<String> tab = new ArrayList<String>(driver.getWindowHandles());
		driver.switchTo().window(tab.get(1));
		Thread.sleep(5000);
    	
		String path = Docs_File_path + "/" + Town + ".png";
		Screenshot fpScreenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000))
				.takeScreenshot(driver);
		ImageIO.write(fpScreenshot.getImage(), "PNG", new File(path));
	}

	public static void AssessorHarwich() throws InterruptedException {
		// Click on Begin Search
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_BtnSearch"))).click();
		Thread.sleep(3000);

		// Enter Street No.
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_TxtLoc1"))).sendKeys(StreetNo);
		// Enter Street No.
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_TxtStreet")))
				.sendKeys(StreetName);

		// Click on Click to Search using criteria below button
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_BtnSearch"))).click();

		try {
			// Click on Click to Search using criteria below button
			wait.until(ExpectedConditions.presenceOfElementLocated(
					By.xpath("//*[@id=\"ctl00_MainContent_Table1\"]/tbody/tr[2]/td[14]/a/img"))).click();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Assessor documents is not available for : " + Town + "&&" + e.getMessage());
		}
	}

	public static void barnstable_Township() throws InterruptedException {
		// click on Enter online database
		wait.until(ExpectedConditions.presenceOfElementLocated(By.name("ctl00$MainContent$btnEnterOnlineDatabase")))
				.click();
		Thread.sleep(3000);
		// Enter address in searchbox
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("MainContent_txtSearchAddress"))).sendKeys(ADD1);
		// click on search
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='SearchAll']/span[7]"))).click();
		Thread.sleep(3000);

		try {
			// click on Address link
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
					"/html/body/form/div[3]/div/div[1]/div[4]/div[1]/div/div/div[2]/div/table/tbody/tr[2]/td[1]/a")))
					.click();
			Thread.sleep(3000);

			// add Ashot jar file to Java project - for maven, add Ashot dependency
			String path = Docs_File_path + "/" + Town + ".png";
			Screenshot fpScreenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000))
					.takeScreenshot(driver);
			ImageIO.write(fpScreenshot.getImage(), "PNG", new File(path));
			Thread.sleep(3000);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Assessor documents is not available for : " + Town + "&&" + e.getMessage());
		}
	}

	public static void CHATHAMandPROVINCE_Township() throws InterruptedException, IOException {
		// StreetName Name
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("input_st_name"))).sendKeys(StreetName);

		// Click on Click to Search using criteria below button
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("input_st_num"))).sendKeys(StreetNo);

		// System.out.println(driver.findElements(By.xpath("//a[@data-title='Click to
		// download the Assessing Summary']")).size());

		for (int p = 0; p <= driver.findElements(By.xpath("//a[@data-title='Click to download the Assessing Summary']"))
				.size(); p++) {

			System.out.println("Document No:" + p);
			Thread.sleep(3000);

			try {
				driver.findElements(By.xpath("//a[@data-title='Click to download the Assessing Summary']")).get(p)
						.click();
				Thread.sleep(3000);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("Assessor documents is not available for : " + Town + "&&" + e.getMessage());
			}
		}

	}

	public static void Eastham_Township() throws InterruptedException, IOException {
		// StreetName
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("input_st_name"))).sendKeys(StreetName);

		// Click on Click to Search using criteria below button
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("input_st_num"))).sendKeys(StreetNo);

		// System.out.println(driver.findElements(By.xpath("//a[@data-title='Click to
		// download the Assessing Summary']")).size());

		for (int p = 0; p <= driver.findElements(By.xpath("//a[@data-title='Click to download the Assessing Summary']"))
				.size(); p++) {

			System.out.println("Document No:" + p);
			Thread.sleep(3000);

			try {
				driver.findElements(By.xpath("//a[@data-title='Click to download the Assessing Summary']")).get(p)
						.click();
				Thread.sleep(3000);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("Assessor documents is not available for : " + Town + "&&" + e.getMessage());
			}
		}

	}

	// Berkshire County methods
	public static void Berkshireprocess() throws InterruptedException {
		System.out.println("**Name search details**");

		// Enter Business/Last Name
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("SearchFormEx1_ACSTextBox_LastName1")))
				.sendKeys(LName);
		Thread.sleep(3000);
		try {
			// Enter First Name
			wait.until(ExpectedConditions
					.presenceOfElementLocated(By.xpath("//input[@id=\"SearchFormEx1_ACSTextBox_FirstName1\"]")))
					.sendKeys(FName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			// Click on Search button
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("SearchFormEx1_btnSearch"))).click();
			Thread.sleep(5000);

			driver.navigate().refresh();
			Thread.sleep(1000);

			// Click on 100/page link
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("DocList1_PageView100Btn"))).click();
			Thread.sleep(2000);

			String page = driver.findElement(By.xpath("//*[@id=\"DocList1_ctl02_ctl00_LinkButtonNumber\"]")).getText();
			System.out.println(page);

			// Pagination loop
			System.out
					.println("No. of pages" + driver.findElements(By.xpath("//a[@class='PagerNumberButton']")).size());
			for (int p = 0; p <= driver.findElements(By.xpath("//a[@class='PagerNumberButton']")).size(); p++) {
				System.out.println("Page No:" + p);
				berkshireDocTypeSelection();
				Thread.sleep(3000);
				driver.findElement(By.xpath("//*[text()='Next']")).click();
				Thread.sleep(3000);
			}

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println("documents are not avilable for names ");
		}

		// Property Search//

		System.out.println("**Address search details**");

		// Click on Search Criteria
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("Navigator1_SearchCriteria1_menuLabel"))).click();
		// Select Property Search
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("Navigator1_SearchCriteria1_LinkButton03")))
				.click();
		Thread.sleep(3000);

		// Enter Street No
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("SearchFormEx1_ACSTextBox_StreetNumber")))
				.sendKeys(StreetNo);
		// Enter Street Name
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("SearchFormEx1_ACSTextBox_StreetName")))
				.sendKeys(StreetName);

		try {
			// Click on Search button
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("SearchFormEx1_btnSearch"))).click();
			Thread.sleep(2000);

			// Click on 100/page link
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("DocList1_PageView100Btn"))).click();
			Thread.sleep(2000);

			// Pagination loop
			System.out
					.println("No. of pages" + driver.findElements(By.xpath("//a[@class='PagerNumberButton']")).size());
			for (int p = 0; p <= driver.findElements(By.xpath("//a[@class='PagerNumberButton']")).size(); p++) {
				System.out.println("Page No:" + p);
				berkshireDocTypeSelection();
				Thread.sleep(3000);
				driver.findElement(By.xpath("//*[text()='Next']")).click();
				Thread.sleep(3000);
			}

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println("documents are not avilable for Address ");
		}
	}

	public static void printBerkshirePDF() throws InterruptedException {
		// click on Print document button in popup
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("DocDetails1_PrintDocBut"))).click();
		Thread.sleep(5000);

		// Print criteria next button
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("PrintCriteriaCtrl1_ImageButton_Next"))).click();
		Thread.sleep(7000);

		// Window handling
		ArrayList<String> tab = new ArrayList<String>(driver.getWindowHandles());
		driver.switchTo().window(tab.get(1));
		Thread.sleep(5000);

		// Click here
		wait.until(ExpectedConditions.presenceOfElementLocated(
				By.xpath("//*[@id=\"PrintPreview1_WidgetContainer1\"]/table/tbody/tr/td/span[2]/a"))).click();
		Thread.sleep(7000);

		// Close current popup window
		driver.close();
		Thread.sleep(2000);

		// go back to main window
		driver.switchTo().window(tab.get(0));
		Thread.sleep(2000);
	}

	public static void berkshireDocTypeSelection() throws InterruptedException {

		// Mortgage Documents

		// Printing Mortgage elements size
		System.out.println("MORTGAGE documents : " + driver
				.findElements(By.xpath("/html/body/form/div[4]/div[27]/div[1]/div[2]/table//*[text()='MORTGAGE']"))
				.size());
		Thread.sleep(3000);
		// driver.findElements(By.xpath("//a[contains(text(),'MORTGAGE')]")).size()
		for (int M = 0; M < driver
				.findElements(By.xpath("/html/body/form/div[4]/div[27]/div[1]/div[2]/table//*[text()='MORTGAGE']"))
				.size(); M++) {
			System.out.println(M);
			Thread.sleep(3000);

			try {
				// Click on only mortgage text in table
				driver.findElements(
						By.xpath("/html/body/form/div[4]/div[27]/div[1]/div[2]/table//*[text()='MORTGAGE']")).get(M)
						.click();
				Thread.sleep(5000);

				printBerkshirePDF();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("Mortgage documents are not available");
				Thread.sleep(3000);
			}

		}

		// Deed documents

		System.out.println("DEED documents:" + driver
				.findElements(By.xpath("/html/body/form/div[4]/div[27]/div[1]/div[2]/table//*[text()='DEED']")).size());
		Thread.sleep(3000);

		for (int D = 0; D < driver
				.findElements(By.xpath("/html/body/form/div[4]/div[27]/div[1]/div[2]/table//*[text()='DEED']"))
				.size(); D++) {
			System.out.println(D);
			Thread.sleep(3000);

			try {
				// Click on Deed in table
				driver.findElements(By.xpath("/html/body/form/div[4]/div[27]/div[1]/div[2]/table//*[text()='DEED']"))
						.get(D).click();
				Thread.sleep(5000);

				printBerkshirePDF();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("Deed documents are not available");
				Thread.sleep(3000);
			}

		}

		// LIEN Documents

		// Printing LIEN elements size
		System.out.println("LIEN documents : " + driver
				.findElements(By.xpath("/html/body/form/div[4]/div[27]/div[1]/div[2]/table//*[text()='CERTIFICATE OF MUNICIPAL LIEN']")).size());
		Thread.sleep(3000);
		// driver.findElements(By.xpath("//a[contains(text(),'MORTGAGE')]")).size()
		for (int LN = 0; LN < driver
				.findElements(By.xpath("/html/body/form/div[4]/div[27]/div[1]/div[2]/table//*[text()='CERTIFICATE OF MUNICIPAL LIEN']"))
				.size(); LN++) {
			System.out.println(LN);
			Thread.sleep(3000);

			try {
				// Click on mortgage in table
				driver.findElements(By.xpath("/html/body/form/div[4]/div[27]/div[1]/div[2]/table//*[text()='CERTIFICATE OF MUNICIPAL LIEN']"))
						.get(LN).click();
				Thread.sleep(5000);

				printBerkshirePDF();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("LIEN documents are not available");
				Thread.sleep(3000);
			}

		}
	}

	// Berkshire Township urls
	public static void Berkshire_Town_URLs() throws InterruptedException {
		String Berkshire_Town_Clarksburg = "Clarksburg";
		String Berkshire_Town_NorthAdams = "North Adams";
		String Berkshire_Town_Adams = "Adams";
		String Berkshire_Town_Alford = "Alford";
		String Berkshire_Town_Becket = "Becket";
		String Berkshire_Town_Egremont = "Egremont";
		String Berkshire_Town_GreatBarrington = "Great Barrington";
		String Berkshire_Town_Lee = "Lee";
		String Berkshire_Town_Peru = "Peru";
		String Berkshire_Town_Richmond = "Richmond";
		String Berkshire_Town_Sheffield = "Sheffield";
		String Berkshire_Town_Washington = "Washington";
		String Berkshire_Town_WestStockbridge = "West Stockbridge";
		String Berkshire_Town_Williamstown = "Williamstown";
		String Berkshire_Town_Windsor = "Windsor";
		String Berkshire_Town_Lenox = "Lenox";
		String Berkshire_Town_NewMarlborough = "New Marlborough";
		String Berkshire_Town_Tyringham = "Tyringham";
		String Berkshire_Town_Hinsdale = "Hinsdale";
		String Berkshire_Town_Savoy = "Savoy";
		String Berkshire_Town_Lanesborough = "Lanesborough";
		String Berkshire_Town_Otis = "Otis";
		String Berkshire_Town_Cheshire = "Cheshire";
		String Berkshire_Town_Dalton = "Dalton";
		String Berkshire_Town_Florida = "Florida";
		String Berkshire_Town_Hancock = "Hancock";
		String Berkshire_Town_Monterey = "Monterey";
		String Berkshire_Town_MountWashington = "Mount Washington";
		String Berkshire_Town_NewAshford = "New Ashford";
		String Berkshire_Town_Pittsfield = "Pittsfield";
		String Berkshire_Town_Sandisfield = "Sandisfield";
		String Berkshire_Town_Stockbridge = "Stockbridge";

		if (Town.equalsIgnoreCase(Berkshire_Town_Clarksburg)) {
			driver.get("http://webpro.patriotproperties.com/clarksburg/default.asp");
			Thread.sleep(5000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Berkshire_Town_NorthAdams)) {
			driver.get("http://northadams.patriotproperties.com/default.asp");
			Thread.sleep(5000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Berkshire_Town_Adams)) {
			driver.get("http://epas.csc-ma.us/PublicAccess/Pages/SearchSales.aspx?town=ADAMS");
			Thread.sleep(5000);

			try {
				Berkshire_epas();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Berkshire_Town_Alford)) {
			driver.get("http://epas.csc-ma.us/PublicAccess/Pages/SearchSales.aspx?town=ALFORD");
			Thread.sleep(5000);

			try {
				Berkshire_epas();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Berkshire_Town_Becket)) {
			driver.get("http://epas.csc-ma.us/publicaccess/Pages/SearchSales.aspx?town=BECKET");
			Thread.sleep(5000);

			try {
				Berkshire_epas();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Berkshire_Town_Egremont)) {
			driver.get("http://epas.csc-ma.us/publicaccess/Pages/SearchSales.aspx?town=Egremont");
			Thread.sleep(5000);

			try {
				Berkshire_epas();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Berkshire_Town_GreatBarrington)) {
			driver.get("http://epas.csc-ma.us/PublicAccess/Pages/SearchSales.aspx?town=CHESTER");
			Thread.sleep(5000);

			try {
				Berkshire_epas();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Berkshire_Town_Lee)) {
			driver.get("http://epas.csc-ma.us/publicaccess/Pages/SearchSales.aspx?town=LEE");
			Thread.sleep(5000);

			try {
				Berkshire_epas();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Berkshire_Town_Peru)) {
			driver.get("http://epas.csc-ma.us/PublicAccess/Pages/AllCommunitySearchSales.aspx");
			Thread.sleep(5000);

			try {
				Berkshire_epas();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Berkshire_Town_Richmond)) {
			driver.get("http://epas.csc-ma.us/PublicAccess/Pages/SearchSales.aspx?town=RICHMOND");
			Thread.sleep(5000);

			try {
				Berkshire_epas();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Berkshire_Town_Sheffield)) {
			driver.get("http://epas.csc-ma.us/PublicAccess/Pages/SearchSales.aspx?town=Sheffield");
			Thread.sleep(5000);

			try {
				Berkshire_epas();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Berkshire_Town_Washington)) {
			driver.get("http://epas.csc-ma.us/PublicAccess/Pages/SearchSales.aspx?town=WASHINGTON");
			Thread.sleep(5000);
			try {
				Berkshire_epas();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Berkshire_Town_WestStockbridge)) {
			driver.get("http://epas.csc-ma.us/PublicAccess/Pages/AllCommunitySearchSales.aspx");
			Thread.sleep(5000);

			try {
				Berkshire_epas();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Berkshire_Town_Williamstown)) {
			driver.get("http://epas.csc-ma.us/PublicAccess/Pages/SearchSales.aspx?town=Williamstown");
			Thread.sleep(5000);

			try {
				Berkshire_epas();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Berkshire_Town_Windsor)) {
			driver.get("http://epas.csc-ma.us/PublicAccess/Pages/SearchSales.aspx?town=WINDSOR");
			Thread.sleep(5000);

			try {
				Berkshire_epas();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Berkshire_Town_Lenox)) {
			driver.get("http://www.assessedvalues2.com/index.aspx?jurcode=152");
			Thread.sleep(5000);

			try {
				Berkshire_Assessedvalues2();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Berkshire_Town_NewMarlborough)) {
			driver.get("http://www.assessedvalues2.com/index.aspx?jurcode=203");
			Thread.sleep(5000);

			try {
				Berkshire_Assessedvalues2();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Berkshire_Town_Tyringham)) {
			driver.get("http://www.assessedvalues2.com/Index.aspx?jurcode=302");
			Thread.sleep(5000);

			try {
				Berkshire_Assessedvalues2();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Berkshire_Town_Hinsdale)) {
			driver.get("http://gis.vgsi.com/hinsdalema/");
			Thread.sleep(5000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Berkshire_Town_Savoy)) {
			driver.get("http://maps.massgis.state.ma.us/map_ol/savoy.php");
			Thread.sleep(5000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Berkshire_Town_Lanesborough)) {
			driver.get("https://www.axisgis.com/LanesboroughMA/");
			Thread.sleep(7000);

			try {
				Berkshire_axis();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Berkshire_Town_Otis)) {
			driver.get(
					"https://www.mapsonline.net/otisma/web_assessor/search.php#sid=f5c2c55b45d7d6b04aa2e9e09498f21f");
			Thread.sleep(5000);

			try {
				Berkshire_Otis();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Berkshire_Town_Cheshire)) {
			driver.get("https://www.propertyrecordcards.com/searchmaster.aspx?towncode=025");
			Thread.sleep(5000);

			try {
				Berkshire_Cheshire();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Berkshire_Town_Dalton)) {
			driver.get("http://www.taxbillsonline.com/Dalton/");
			Thread.sleep(5000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Berkshire_Town_Florida)) {
			driver.get("https://www.mass.gov/service-details/massachusetts-interactive-property-map");
			Thread.sleep(5000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Berkshire_Town_Hancock)) {
			driver.get("http://town.hancock.ma.us/2019/02/20/assessors/");
			Thread.sleep(5000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Berkshire_Town_Monterey)) {
			driver.get("https://www.axisgis.com/MontereyMA/");
			Thread.sleep(7000);

			try {
				Berkshire_axis();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Berkshire_Town_MountWashington)) {
			driver.get("https://townofmtwashington.com/assessor/");
			Thread.sleep(5000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Berkshire_Town_NewAshford)) {
			driver.get("http://newashford.patriotproperties.com/default.asp");
			Thread.sleep(5000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Berkshire_Town_Pittsfield)) {
			driver.get("http://host.appgeo.com/PittsfieldMA/");
			Thread.sleep(5000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Berkshire_Town_Sandisfield)) {
			driver.get("https://www.axisgis.com/SandisfieldMA/");
			Thread.sleep(5000);

			try {
				Berkshire_axis();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Berkshire_Town_Stockbridge)) {
			driver.get("http://townofstockbridge.com/wp/town-government/assessors/");
			Thread.sleep(5000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

	}

	public static void Berkshire_epas() throws InterruptedException {

		// Enter Street No
		wait.until(ExpectedConditions.presenceOfElementLocated(By.name("ctl00$body$txtStreetNo"))).sendKeys(StreetNo);
		// Enter Street Name
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_body_txtStreetName"))).sendKeys(StreetName);

		// Click on Find
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_body_btnFind"))).click();
		Thread.sleep(5000);
		// Click on result link
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_body_grdSalesSummary_ctl03_lnkParcelID")))
				.click();
		Thread.sleep(5000);

		// Click on view print tab
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("104"))).click();

		driver.navigate().back();
	}

	public static void Berkshire_axis() throws InterruptedException {
		// Click on Agree
		// wait.until(ExpectedConditions.presenceOfElementLocated(By.id("closeSplash"))).click();

		try {
			// Click on Search box
			wait.until(ExpectedConditions
					.presenceOfElementLocated(By.xpath("//*[@id='slide-in-search-handle']/div[2]/div"))).click();
			// Enter Address in Search box
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("searchInput"))).sendKeys(ADD1);
			// Thread.sleep(5000);

			// Click on result
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='searchGrid']/tbody/tr/td[2]")))
					.click();

			Robot r = new Robot();
			r.keyPress(KeyEvent.VK_TAB);
			r.keyRelease(KeyEvent.VK_TAB);
			Thread.sleep(2000);
			r.keyPress(KeyEvent.VK_TAB);
			r.keyRelease(KeyEvent.VK_TAB);
			Thread.sleep(2000);

			// Click on result window
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='parcelDetailTab']/ul/li[1]/a")))
					.click();
			Thread.sleep(2000);

			// Click on property card
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='CAI Property Card']")))
					.click();
			Thread.sleep(2000);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}

	public static void Berkshire_Otis() throws InterruptedException, IOException {
		// StreetName Name
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("input_st_name"))).sendKeys(StreetName);

		// Click on Click to Search using criteria below button
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("input_st_num"))).sendKeys(StreetNo);

		// System.out.println(driver.findElements(By.xpath("//a[@data-title='Click to
		// download the Assessing Summary']")).size());

		for (int p = 0; p <= driver.findElements(By.xpath("//*[@data-reportname=\"Assessor's Property Report\"]"))
				.size(); p++) {

			System.out.println("Document No:" + p);
			Thread.sleep(5000);

			try {
				driver.findElements(By.xpath("//*[@data-reportname=\"Assessor's Property Report\"]")).get(p).click();
				Thread.sleep(5000);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("Assessor documents is not available for : " + Town + "&&" + e.getMessage());
			}
		}

	}

	public static void Berkshire_Cheshire() throws InterruptedException, AWTException, IOException {
		String StreetNo = "710";
		String StreetName = "ALLEN AVE";

		// Click on Street Number
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("MainContent_tbPropertySearchStreetNumber")))
				.sendKeys(StreetNo);

		// Click on Street Name
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("MainContent_cbPropertySearchStreetName_chzn")))
				.click();

		// Enter on Street Name
		wait.until(ExpectedConditions.presenceOfElementLocated(
				By.xpath("//*[@id='MainContent_cbPropertySearchStreetName_chzn']/div/div/input"))).sendKeys(StreetName);
		Thread.sleep(2000);

		// Enter on Street Name
		wait.until(ExpectedConditions.presenceOfElementLocated(
				By.xpath("//*[@id='MainContent_cbPropertySearchStreetName_chzn']/div/div/input"))).sendKeys(Keys.ENTER);

		// Click on search
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("MainContent_btnPropertySearch"))).click();
		Thread.sleep(5000);
		// CLick on reult link
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='dt_a']/tbody/tr/td[1]/a"))).click();
		// CLick on Print view button
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='form1']/div[4]/div/div[2]/div[2]/a")))
				.click();

		// Window handling
		ArrayList<String> Cheshiretab = new ArrayList<String>(driver.getWindowHandles());
		driver.switchTo().window(Cheshiretab.get(1));
		Thread.sleep(5000);

		String path = Docs_File_path + "/" + Town + ".png";
		Screenshot fpScreenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000))
				.takeScreenshot(driver);
		ImageIO.write(fpScreenshot.getImage(), "PNG", new File(path));
		Thread.sleep(5000);

		driver.close();

		driver.switchTo().window(Cheshiretab.get(0));

		driver.navigate().back();

	}

	public static void Berkshire_Assessedvalues2() throws InterruptedException {
		// Click on Begin Search
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_BtnSearch"))).click();
		Thread.sleep(3000);

		// Enter Street No.
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_TxtLoc1"))).sendKeys(StreetNo);
		// Enter Street No.
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_TxtStreet")))
				.sendKeys(StreetName);

		// Click on Click to Search using criteria below button
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_BtnSearch"))).click();

		try {
			// Click on Click to Search using criteria below button
			wait.until(ExpectedConditions.presenceOfElementLocated(
					By.xpath("//*[@id=\"ctl00_MainContent_Table1\"]/tbody/tr[2]/td[14]/a/img"))).click();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Assessor documents is not available for : " + Town + "&&" + e.getMessage());
		}
	}

	// Bristol Fall River and Southern process
	public static void Bristol_fallriver_process() {
		System.out.println("**Name search details**");

		try {
			// Enter Business/Last Name
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("SearchFormEx1_ACSTextBox_LastName1")))
					.sendKeys(LName);
			Thread.sleep(3000);
			// Enter First Name
			wait.until(ExpectedConditions
					.presenceOfElementLocated(By.xpath("//input[@id=\"SearchFormEx1_ACSTextBox_FirstName1\"]")))
					.sendKeys(FName);

			try {
				// Click on Search button
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("SearchFormEx1_btnSearch"))).click();
				Thread.sleep(5000);

				driver.navigate().refresh();
				Thread.sleep(1000);

				// Click on 100/page link
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("DocList1_PageView100Btn"))).click();
				Thread.sleep(2000);

				String page = driver.findElement(By.xpath("//*[@id=\"DocList1_ctl02_ctl00_LinkButtonNumber\"]"))
						.getText();
				System.out.println(page);

				System.out.println(
						"No. of pages" + driver.findElements(By.xpath("//a[@class='PagerNumberButton']")).size());
				for (int p = 0; p <= driver.findElements(By.xpath("//a[@class='PagerNumberButton']")).size(); p++) {
					System.out.println("Page No:" + p);
					bristolDocTypeSelection();
					Thread.sleep(3000);
					driver.findElement(By.xpath("//*[text()='Next']")).click();
					Thread.sleep(3000);
				}

			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.out.println("documents are not avilable for names ");
			}

			System.out.println("**Address search details**"); // Property Search//

			// Click on Search Criteria
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("Navigator1_SearchCriteria1_menuLabel")))
					.click();
			// Select Property Search
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("Navigator1_SearchCriteria1_LinnkButton_14")))
					.click();
			Thread.sleep(3000);

			// Enter Street No
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("SearchFormEx1_ACSTextBox_StreetNumber")))
					.sendKeys(StreetNo);
			// Enter Street Name
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("SearchFormEx1_ACSTextBox_StreetName")))
					.sendKeys(StreetName);

			try {
				// Click on Search button
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("SearchFormEx1_btnSearch"))).click();
				Thread.sleep(2000);

				// Click on 100/page link
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("DocList1_PageView100Btn"))).click();
				Thread.sleep(2000);

				System.out.println(
						"No. of pages" + driver.findElements(By.xpath("//a[@class='PagerNumberButton']")).size());
				for (int p = 0; p <= driver.findElements(By.xpath("//a[@class='PagerNumberButton']")).size(); p++) {
					System.out.println("Page No:" + p);
					bristolDocTypeSelection();
					Thread.sleep(3000);
					driver.findElement(By.xpath(" 	")).click();
					Thread.sleep(3000);
				}

			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.out.println("documents are not avilable for Address ");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Please check the site is available or " + e.getMessage());
		}

	}

	public static void printbristolPDF() throws InterruptedException {
		// click on Print document button in popup
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("DocDetails1_Btn_PrintDoc"))).click();
		Thread.sleep(5000);

		// Print criteria next button
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("PrintCriteriaCtrl1_ImageButton_Next"))).click();
		Thread.sleep(7000);

		// Window handling
		ArrayList<String> tab = new ArrayList<String>(driver.getWindowHandles());
		driver.switchTo().window(tab.get(1));
		Thread.sleep(5000);

		// Subscriber details are asking

		// Click here
		wait.until(ExpectedConditions.presenceOfElementLocated(
				By.xpath("//*[@id=\"PrintPreview1_WidgetContainer1\"]/table/tbody/tr/td/span[2]/a"))).click();
		Thread.sleep(7000);

		// Close current popup window
		driver.close();
		Thread.sleep(2000);

		// go back to main window
		driver.switchTo().window(tab.get(0));
		Thread.sleep(2000);
	}

	public static void bristolDocTypeSelection() throws InterruptedException {

		// Mortgage Documents

		// Printing Mortgage elements size
		System.out.println("MORTGAGE documents : " + driver
				.findElements(
						By.xpath("//*[@id='DocList1_ContentContainer1']/table/tbody/tr[1]/td//*[text()='MORTGAGE']"))
				.size());
		Thread.sleep(3000);
		// driver.findElements(By.xpath("//a[contains(text(),'MORTGAGE')]")).size()
		for (int M = 0; M < driver
				.findElements(
						By.xpath("//*[@id='DocList1_ContentContainer1']/table/tbody/tr[1]/td//*[text()='MORTGAGE']"))
				.size(); M++) {
			System.out.println(M);
			Thread.sleep(3000);

			try {
				// Click on only mortgage text in table
				driver.findElements(
						By.xpath("//*[@id='DocList1_ContentContainer1']/table/tbody/tr[1]/td//*[text()='MORTGAGE']"))
						.get(M).click();
				Thread.sleep(5000);

				printbristolPDF();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("Mortgage documents are not available");
				Thread.sleep(3000);
			}

		}

		// Deed documents

		System.out.println("DEED documents:" + driver
				.findElements(By.xpath("//*[@id='DocList1_ContentContainer1']/table/tbody/tr[1]/td//*[text()='DEED']"))
				.size());
		Thread.sleep(3000);

		for (int D = 0; D < driver
				.findElements(By.xpath("//*[@id='DocList1_ContentContainer1']/table/tbody/tr[1]/td//*[text()='DEED']"))
				.size(); D++) {
			System.out.println(D);
			Thread.sleep(3000);

			try {
				// Click on Deed in table
				driver.findElements(
						By.xpath("//*[@id='DocList1_ContentContainer1']/table/tbody/tr[1]/td//*[text()='DEED']")).get(D)
						.click();
				Thread.sleep(5000);

				printbristolPDF();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("Deed documents are not available");
				Thread.sleep(3000);
			}

		}

		// LIEN Documents

		// Printing LIEN elements size
		System.out.println("LIEN documents : " + driver
				.findElements(By.xpath("//*[@id='DocList1_ContentContainer1']/table/tbody/tr[1]/td//*[text()='CERTIFICATE OF MUNICIPAL LIEN']"))
				.size());
		Thread.sleep(3000);
		// driver.findElements(By.xpath("//a[contains(text(),'MORTGAGE')]")).size()
		for (int LN = 0; LN < driver
				.findElements(By.xpath("//*[@id='DocList1_ContentContainer1']/table/tbody/tr[1]/td//*[text()='CERTIFICATE OF MUNICIPAL LIEN']"))
				.size(); LN++) {
			System.out.println(LN);
			Thread.sleep(3000);

			try {
				// Click on mortgage in table
				driver.findElements(
						By.xpath("//*[@id='DocList1_ContentContainer1']/table/tbody/tr[1]/td//*[text()='CERTIFICATE OF MUNICIPAL LIEN']"))
						.get(LN).click();
				Thread.sleep(5000);

				printbristolPDF();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("LIEN documents are not available");
				Thread.sleep(3000);
			}

		}
	}

	public static void Bristol_Sothern_process() {
		System.out.println("**Name search details**");

		try {
			// Enter Business/Last Name
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("SearchFormEx1_ACSTextBox_LastName1")))
					.sendKeys(LName);
			Thread.sleep(3000);
			// Enter First Name
			wait.until(ExpectedConditions
					.presenceOfElementLocated(By.xpath("//input[@id=\"SearchFormEx1_ACSTextBox_FirstName1\"]")))
					.sendKeys(FName);

			try {
				// Click on Search button
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("SearchFormEx1_btnSearch"))).click();
				Thread.sleep(5000);

				driver.navigate().refresh();
				Thread.sleep(1000);

				// Click on 100/page link
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("DocList1_PageView100Btn"))).click();
				Thread.sleep(2000);

				String page = driver.findElement(By.xpath("//*[@id=\"DocList1_ctl02_ctl00_LinkButtonNumber\"]"))
						.getText();
				System.out.println(page);

				System.out.println(
						"No. of pages" + driver.findElements(By.xpath("//a[@class='PagerNumberButton']")).size());
				for (int p = 0; p <= driver.findElements(By.xpath("//a[@class='PagerNumberButton']")).size(); p++) {
					System.out.println("Page No:" + p);
					bristolDocTypeSelection();
					Thread.sleep(3000);
					driver.findElement(By.xpath("//*[text()='Next']")).click();
					Thread.sleep(3000);
				}

			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.out.println("documents are not avilable for names ");
			}

			System.out.println("**Address search details**"); // Property Search//

			// Click on Search Criteria
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("Navigator1_SearchCriteria1_menuLabel")))
					.click();
			// Select Property Search
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("Navigator1_SearchCriteria1_LinnkButton_14")))
					.click();
			Thread.sleep(3000);

			// Enter Street No
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("SearchFormEx1_ACSTextBox_StreetNumber")))
					.sendKeys(StreetNo);
			// Enter Street Name
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("SearchFormEx1_ACSTextBox_StreetName")))
					.sendKeys(StreetName);

			try {
				// Click on Search button
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("SearchFormEx1_btnSearch"))).click();
				Thread.sleep(2000);

				// Click on 100/page link
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("DocList1_PageView100Btn"))).click();
				Thread.sleep(2000);

				System.out.println(
						"No. of pages" + driver.findElements(By.xpath("//a[@class='PagerNumberButton']")).size());
				for (int p = 0; p <= driver.findElements(By.xpath("//a[@class='PagerNumberButton']")).size(); p++) {
					System.out.println("Page No:" + p);
					bristolDocTypeSelection();
					Thread.sleep(3000);
					driver.findElement(By.xpath(" 	")).click();
					Thread.sleep(3000);
				}

			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.out.println("documents are not avilable for Address ");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Please check the site is available or " + e.getMessage());
		}
	}

	public static void Bristol_North_process() throws InterruptedException {
		try {
			// click on Recorded Land Street tab
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"left\"]/ul[1]/li[2]/a")))
					.click(); // *[@id="left"]/ul[1]/li[2]/a

			// Enter Street Number
			wait.until(ExpectedConditions
					.presenceOfElementLocated(By.id("ctl00_cphMainContent_txtStreetNumber_vtbTextBox_text")))
					.sendKeys(StreetNo);
			// Enter Street Nmae
			wait.until(ExpectedConditions
					.presenceOfElementLocated(By.id("ctl00_cphMainContent_txtStreetName_vtbTextBox_text")))
					.sendKeys(StreetNo);

			/*
			 * //Identify Doc Type dropdown and values 100183 ,100517 , 100708, selected
			 * WebElement DocTypeDD; DocTypeDD=driver.findElement(By.id(
			 * "ctl00_cphMainContent_ddlDocType_vddlDropDown")); Select s1=new
			 * Select(DocTypeDD); String DD = "selected";
			 */

			// Click on Search button
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_cphMainContent_btnSearchName")))
					.click();
			Thread.sleep(3000);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Please check the site is available or " + e.getMessage());
		}
	}

	// Bristol Township urls
	public static void Bristol_Town_URLs() throws InterruptedException {
		WebDriverManager.chromedriver().setup();
		// System.setProperty("webdriver.chrome.driver",
		// "C:\\AUTOMATION\\Selenium\\Drivers\\chromedriver.exe");
		HashMap<String, Object> chromePref = new HashMap<String, Object>();
		chromePref.put("plugins.always_open_pdf_externally", true);
		chromePref.put("profile.default_content_settings.popup", 0);
		chromePref.put("download.default_directory", Docs_File_path);
		ChromeOptions options = new ChromeOptions();
		options.setExperimentalOption("prefs", chromePref);
		driver = new ChromeDriver(options);
		driver.manage().window().maximize();

		wait = new WebDriverWait(driver, 40);

		String Bristol_Town_Acushnet = "Acushnet";
		String Bristol_Town_Fairhaven = "Fairhaven";
		String Bristol_Town_FallRiver = "Fall River";
		String Bristol_Town_Raynham = "Raynham";
		String Bristol_Town_Berkley = "Berkley";
		String Bristol_Town_Somerset = "Somerset";
		String Bristol_Town_Freetown = "Freetown";
		String Bristol_Town_Norton = "Norton";
		String Bristol_Town_Westport = "Westport";
		String Bristol_Town_NorthAttleboro = "North Attleboro";
		String Bristol_Town_Dartmouth = "Dartmouth";
		String Bristol_Town_Swansea = "Swansea";
		String Bristol_Town_Taunton = "Taunton";
		String Bristol_Town_Dighton = "Dighton";
		String Bristol_Town_Mansfield = "Mansfield";
		String Bristol_Town_Attleboro = "Attleboro";
		String Bristol_Town_Easton = "Easton";
		String Bristol_Town_NewBedford = "New Bedford";
		String Bristol_Town_Rehoboth = "Rehoboth";
		String Bristol_Town_Seekonk = "Seekonk";

		if (Town.equals(Bristol_Town_Acushnet)) {
			driver.get("http://acushnet.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equals(Bristol_Town_Fairhaven)) {
			driver.get("http://fairhaven.patriotproperties.com/default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equals(Bristol_Town_FallRiver)) {
			driver.get("http://fallriver.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equals(Bristol_Town_Raynham)) {
			driver.get("http://raynham.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equals(Bristol_Town_Berkley)) {
			driver.get("http://epas.csc-ma.us/PublicAccess/Pages/SearchSales.aspx?town=BERKLEY");
			Thread.sleep(3000);

			try {
				Bristol_epas();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equals(Bristol_Town_Somerset)) {
			driver.get("http://epas.csc-ma.us/PublicAccess/Pages/SearchSales.aspx?town=SOMERSET");
			Thread.sleep(3000);

			try {
				Bristol_epas();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equals(Bristol_Town_Freetown)) {
			driver.get("http://assessedvalues2.com/SearchPage.aspx?jurcode=102");
			Thread.sleep(3000);

			try {
				Bristol_Assessedvalues2();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equals(Bristol_Town_Norton)) {
			driver.get("http://www.assessedvalues2.com/index.aspx?jurcode=218");
			Thread.sleep(3000);

			try {
				Bristol_Assessedvalues2();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equals(Bristol_Town_Westport)) {
			driver.get("http://www.assessedvalues2.com/index.aspx?jurcode=334");
			Thread.sleep(3000);

			try {
				Bristol_Assessedvalues2();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equals(Bristol_Town_NorthAttleboro)) {
			driver.get("http://gis.vgsi.com/northattleboroma/");
			Thread.sleep(3000);

			try {
				Bristol_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equals(Bristol_Town_Dartmouth)) {
			driver.get("http://gis.vgsi.com/dartmouthma/");
			Thread.sleep(3000);

			try {
				Bristol_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equals(Bristol_Town_Swansea)) {
			driver.get("http://gis.vgsi.com/Swanseama/");
			Thread.sleep(3000);

			try {
				Bristol_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equals(Bristol_Town_Taunton)) {
			driver.get("http://gis.vgsi.com/Tauntonma/");
			Thread.sleep(3000);

			try {
				Bristol_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equals(Bristol_Town_Dighton)) {
			driver.get("https://www.axisgis.com/dightonma/");
			Thread.sleep(3000);

			try {
				Bristol_axis();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equals(Bristol_Town_Mansfield)) {
			driver.get("https://gis.mansfieldma.com/parcels/parcelsearch.aspx");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equals(Bristol_Town_Attleboro)) {
			driver.get("https://nereval.com/?town=Attleboro&AspxAutoDetectCookieSupport=1");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equals(Bristol_Town_Easton)) {
			driver.get("http://www.easton.ma.us/departments/treasurer_collector_s_office/index.php");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equals(Bristol_Town_NewBedford)) {
			driver.get("http://www.newbedford-ma.gov/Assessors/RealProperty/RealpropertyLookup.cfm");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equals(Bristol_Town_Rehoboth)) {
			driver.get(
					"https://rehobothma.mapgeo.io/datasets/properties?abuttersDistance=100&latlng=41.843015%2C-71.245396&modal=disclaimer");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equals(Bristol_Town_Seekonk)) {
			driver.get("http://www.seekonk-ma.gov/pages/SeekonkMA_Assessors/index");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
	}

	public static void Bristol_epas() throws InterruptedException {
		// Enter Street No
		wait.until(ExpectedConditions.presenceOfElementLocated(By.name("ctl00$body$txtStreetNo"))).sendKeys(StreetNo);
		// Enter Street Name
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_body_txtStreetName"))).sendKeys(StreetName);

		// Click on Find
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_body_btnFind"))).click();
		Thread.sleep(5000);
		// Click on result link
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_body_grdSalesSummary_ctl03_lnkParcelID")))
				.click();
		Thread.sleep(5000);

		// Click on view print tab
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("104"))).click();

		driver.navigate().back();
	}

	public static void Bristol_Assessedvalues2() throws InterruptedException {
		// Click on Begin Search
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_BtnSearch"))).click();
		Thread.sleep(3000);

		// Enter Street No.
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_TxtLoc1"))).sendKeys(StreetNo);
		// Enter Street Name.
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_TxtStreet")))
				.sendKeys(StreetName);

		// Click on Click to Search using criteria below button
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_BtnSearch"))).click();

		try {
			// Click on card PDF
			wait.until(ExpectedConditions
					.presenceOfElementLocated(By.xpath("//*[@id='ctl00_MainContent_Table1']/tbody/tr[2]/td[14]/a")))
					.click();
		} catch (Exception e) {
			// Click on card PDF
			wait.until(ExpectedConditions
					.presenceOfElementLocated(By.xpath("/html/body/form/div[3]/div[2]/table/tbody/tr[2]/td[15]/a")))
					.click();
		}
	}

	public static void Bristol_GIS() throws InterruptedException, IOException {
		// click on Enter online database
		wait.until(ExpectedConditions.presenceOfElementLocated(By.name("ctl00$MainContent$btnEnterOnlineDatabase")))
				.click();

		// Enter address in searchbox
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("MainContent_txtSearchAddress"))).sendKeys(ADD1);
		// click on search
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='SearchAll']/span[7]"))).click();

		// click on Address link
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='MainContent_grdSearchResults']/tbody/tr[2]/td[1]/a")))
				.click();
		Thread.sleep(2000);

		String path = Docs_File_path + "/" + Town + ".png";
		Screenshot fpScreenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000))
				.takeScreenshot(driver);
		ImageIO.write(fpScreenshot.getImage(), "PNG", new File(path));

	}

	public static void Bristol_axis() throws InterruptedException {
		Thread.sleep(5000);
		// Click on Agree
		// wait.until(ExpectedConditions.presenceOfElementLocated(By.id("closeSplash"))).click();

		try {
			// Click on Search box
			wait.until(ExpectedConditions
					.presenceOfElementLocated(By.xpath("//*[@id='slide-in-search-handle']/div[2]/div"))).click();
			// Enter Address in Search box
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("searchInput"))).sendKeys(ADD1);
			// Thread.sleep(3000);

			// Click on result
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='searchGrid']/tbody/tr/td[2]")))
					.click();

			Robot r = new Robot();
			r.keyPress(KeyEvent.VK_TAB);
			r.keyRelease(KeyEvent.VK_TAB);
			Thread.sleep(2000);
			r.keyPress(KeyEvent.VK_TAB);
			r.keyRelease(KeyEvent.VK_TAB);
			Thread.sleep(2000);

			// Click on result window
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='parcelDetailTab']/ul/li[1]/a")))
					.click();

			try {
				// Click on property card
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='CAI Property Card']")))
						.click();
				Thread.sleep(2000);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String Worcester_Town_Rutland = "Rutland";
			String Worcester_Town_WestBoylston = "West Boylston";
			if (Town.equals(Worcester_Town_Rutland) || Town.equals(Worcester_Town_WestBoylston)) {
				// Click on property card
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='Vision_Property_Card']")))
						.click();
				Thread.sleep(2000);
			}

			/*
			 * if(Town.equals(Middlesex_Town_Lincoln)){ //Click on property card
			 * wait.until(ExpectedConditions.presenceOfElementLocated(By.
			 * xpath("//*[text()='Property Card 1']"))).click(); Thread.sleep(2000); }
			 * if(Town.equals(Middlesex_Town_Boxborough)){ //Click on property card
			 * wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
			 * "//*[text()='Patriot_Property_Card']"))).click(); Thread.sleep(2000); }
			 */

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}

	// Dukes Township urls
	public static void Dukes_Town_URLs() throws InterruptedException {
		String Dukes_Town_Chilmark = "Chilmark";
		String Dukes_Town_Edgartown = "Edgartown";
		String Dukes_Town_Gosnold = "Gosnold";
		String Dukes_Town_Aquinnah = "Aquinnah";
		String Dukes_Town_Tisbury = "Tisbury";
		String Dukes_Town_WestTisbury = "West Tisbury";
		String Dukes_Town_GayHead = "Gay Head";
		String Dukes_Town_OakBluffs = "Oak Bluffs";

		if (Town.equalsIgnoreCase(Dukes_Town_Chilmark)) {
			driver.get("http://www.assessedvalues2.com/index.aspx?jurcode=62");
			Thread.sleep(5000);

			try {
				Dukes_assessedvalues2();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Dukes_Town_Edgartown)) {
			driver.get("http://gis.vgsi.com/edgartownma/");
			Thread.sleep(5000);

			try {
				Dukes_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Dukes_Town_Gosnold)) {
			driver.get("http://gis.vgsi.com/gosnoldma/");
			Thread.sleep(3000);

			try {
				Dukes_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Dukes_Town_Aquinnah)) {
			driver.get("https://www.axisgis.com/AquinnahMA/");
			Thread.sleep(7000);

			try {
				Dukes_axis();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Dukes_Town_Tisbury)) {
			driver.get("https://www.axisgis.com/TisburyMA/");
			Thread.sleep(7000);

			try {
				Dukes_axis();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Dukes_Town_WestTisbury)) {
			driver.get("https://www.axisgis.com/West_TisburyMA/");
			Thread.sleep(7000);

			try {
				Dukes_axis();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Dukes_Town_GayHead)) {
			driver.get("https://dukescountydeeds.com/assessors-links/");
			Thread.sleep(2000);

			System.out.println("No documents are available for : " + Town + ": " + driver.getTitle());
		}

		if (Town.equalsIgnoreCase(Dukes_Town_OakBluffs)) {
			driver.get("https://www.mapsonline.net/oakbluffsma/index.html");
			Thread.sleep(5000);

			try {
				Dukes_Mapsonline_Red();

				Robot r = new Robot();
				r.keyPress(KeyEvent.VK_TAB);
				r.keyRelease(KeyEvent.VK_TAB);
				Thread.sleep(2000);
				r.keyPress(KeyEvent.VK_TAB);
				r.keyRelease(KeyEvent.VK_TAB);
				Thread.sleep(2000);
				r.keyPress(KeyEvent.VK_TAB);
				r.keyRelease(KeyEvent.VK_TAB);
				Thread.sleep(2000);

				r.keyPress(KeyEvent.VK_ENTER);
				r.keyRelease(KeyEvent.VK_ENTER);
				Thread.sleep(2000);

				// Click on Open button
				// wait.until(ExpectedConditions.presenceOfElementLocated(By.id("open-button"))).click();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
	}

	public static void Dukes_GIS() throws InterruptedException, AWTException, IOException {
		// click on Enter online database
		wait.until(ExpectedConditions.presenceOfElementLocated(By.name("ctl00$MainContent$btnEnterOnlineDatabase")))
				.click();
		Thread.sleep(3000);
		// Enter address in searchbox
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("MainContent_txtSearchAddress"))).sendKeys(ADD1);
		// click on search
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='SearchAll']/span[7]"))).click();
		Thread.sleep(3000);
		// click on Address link
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='MainContent_grdSearchResults']/tbody/tr[2]/td[1]/a")))
				.click();
		Thread.sleep(3000);

		String path = Docs_File_path + "/" + Town + ".png";
		Screenshot fpScreenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000))
				.takeScreenshot(driver);
		ImageIO.write(fpScreenshot.getImage(), "PNG", new File(path));

	}

	public static void Dukes_assessedvalues2() throws InterruptedException {
		// Click on Begin Search
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_BtnSearch"))).click();
		Thread.sleep(3000);

		// Enter Street No.
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_TxtLoc1"))).sendKeys(StreetNo);
		// Enter Street No.
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_TxtStreet")))
				.sendKeys(StreetName);

		// Click on Click to Search using criteria below button
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_BtnSearch"))).click();

		try {
			// Click on Click to Search using criteria below button
			wait.until(ExpectedConditions.presenceOfElementLocated(
					By.xpath("//*[@id=\"ctl00_MainContent_Table1\"]/tbody/tr[2]/td[14]/a/img"))).click();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Assessor documents is not available for : " + Town + "&&" + e.getMessage());
		}
	}

	public static void Dukes_axis() throws InterruptedException {
		// Click on Agree
		// wait.until(ExpectedConditions.presenceOfElementLocated(By.id("closeSplash"))).click();

		try {
			// Click on Search box
			wait.until(ExpectedConditions
					.presenceOfElementLocated(By.xpath("//*[@id='slide-in-search-handle']/div[2]/div"))).click();
			// Enter Address in Search box
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("searchInput"))).sendKeys(ADD1);
			// Thread.sleep(5000);

			// Click on result
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='searchGrid']/tbody/tr/td[2]")))
					.click();

			Robot r = new Robot();
			r.keyPress(KeyEvent.VK_TAB);
			r.keyRelease(KeyEvent.VK_TAB);
			Thread.sleep(2000);
			r.keyPress(KeyEvent.VK_TAB);
			r.keyRelease(KeyEvent.VK_TAB);
			Thread.sleep(2000);

			// Click on result window
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='parcelDetailTab']/ul/li[1]/a")))
					.click();
			Thread.sleep(2000);

			// Click on property card
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='CAI Property Card']")))
					.click();
			Thread.sleep(2000);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}

	public static void Dukes_Mapsonline_Red() throws InterruptedException, AWTException, IOException {
		// Click on Disclaimer
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("fancybox-close"))).click();

		// click on Find tab
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='divInfo']/div/div[1]/div"))).click();
		Thread.sleep(2000);
		// Enter on Street name
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='addr_trip_search']/table/tbody/tr/td[1]/span/input")))
				.sendKeys(StreetName);
		Thread.sleep(2000);
		// click on Street name
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/ul[1]/li/a"))).click();
		// Enter on Street No
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='addr_trip_search']/table/tbody/tr/td[2]/span/input[1]")))
				.sendKeys(StreetNo);
		Thread.sleep(2000);
		// click on Street No
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/ul[2]/li/a"))).click();
		// Click on result
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='divQueryResultsListMulti']/div/table/tbody/tr/td[1]")))
				.click();
		try {
			// click on property card link
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='CLICK TO PRINT']"))).click();
			Thread.sleep(5000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// click on property card link
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='Open']"))).click();

		}

	}

	// EssexSouth
	public static void Essex_process() throws InterruptedException, AWTException {
		String StartDate = "1/2/1951";

		System.out.println("**Name search details**");

		// Enter StartDate
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='ASPxPageControl1_RecordedTab_NameSearchStart']")))
				.sendKeys(StartDate);
		// Enter Last Name or Business Name
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ASPxPageControl1_RecordedTab_txtNSLastName")))
				.sendKeys(LName);
		// Enter First Name
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ASPxPageControl1_RecordedTab_txtNSFirstName")))
				.sendKeys(FName);
		// click on search
		wait.until(ExpectedConditions.presenceOfElementLocated(By.name("ASPxPageControl1$RecordedTab$cmdNameSearch")))
				.click();

		try {
			// Enter Town
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ASPxGridView1_DXFREditorcol5_I")))
					.sendKeys(Town);

			// Search result and download process
			EssexSouthImgLink();
			Thread.sleep(3000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			driver.navigate().back();
		}

		System.out.println("**Address search details**");

		driver.get("https://salemdeeds.com/salemdeeds/Defaultsearch2.aspx");
		Thread.sleep(3000);

		// Click on Street Search menu
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='ASPxPageControl1_RecordedTab_T1T']/span"))).click();

		// Enter StartDate
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='ASPxPageControl1_RecordedTab_AddressSearchStart']")))
				.sendKeys(StartDate);

		// Enter StreetName
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ASPxPageControl1_RecordedTab_txtStreetName")))
				.sendKeys(StreetName);
		// Enter StreetNo
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ASPxPageControl1_RecordedTab_txtStreetNum")))
				.sendKeys(StreetNo);
		// click on search
		wait.until(
				ExpectedConditions.presenceOfElementLocated(By.name("ASPxPageControl1$RecordedTab$cmdAddressSearch")))
				.click();

		try {
			// Enter Town
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ASPxGridView1_DXFREditorcol5_I")))
					.sendKeys(Town);

			// Search result and download process
			EssexSouthImgLink();
			Thread.sleep(3000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			driver.navigate().back();
		}
	}

	public static void EssexSouthImgLink() throws InterruptedException {
		String ESValues = "MTG,deed,ML";
		String ESouth[] = ESValues.split(",");

		for (int ESi = 0; ESi < ESouth.length; ESi++) {
			System.out.println("Essex South document Type is : " + ESouth[ESi]);
			String ESvalue = ESouth[ESi];

			// Enter type
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ASPxGridView1_DXFREditorcol8_I"))).clear();
			Thread.sleep(3000);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ASPxGridView1_DXFREditorcol8_I")))
					.sendKeys(ESvalue);
			Thread.sleep(3000);

			System.out.println(
					"Documents available : " + driver.findElements(By.xpath("//*[@class='dxeHyperlink']")).size());
			for (int ES = 0; ES < driver.findElements(By.xpath("//*[@class='dxeHyperlink']")).size(); ES++) {
				System.out.println("Document No : " + ES);
				Thread.sleep(3000);

				// Click on Imag link
				driver.findElements(By.xpath("//*[@class='dxeHyperlink']")).get(ES).click();

				// Window handling
				ArrayList<String> tab = new ArrayList<String>(driver.getWindowHandles());
				driver.switchTo().window(tab.get(1));
				Thread.sleep(5000);

				// click on PDF radio button
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("rbImageFormat_1"))).click();

				// click on DownloadPDF button
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("cmdPrintDoc"))).click();

				driver.close();
				// Thread.sleep(3000);
				driver.switchTo().window(tab.get(0));
				Thread.sleep(3000);
			}
		}

	}

	// Essex Township urls
	public static void Essex_Town_URLs() throws InterruptedException {

		String Essex_Town_NorthAndover = "North Andover";
		String Essex_Town_Boxford = "Boxford";
		String Essex_Town_Amesbury = "Amesbury";
		String Essex_Town_Gloucester = "Gloucester";
		String Essex_Town_Hamilton = "Hamilton";
		String Essex_Town_Lawrence = "Lawrence";
		String Essex_Town_Newburyport = "Newburyport";
		String Essex_Town_Rowley = "Rowley";
		String Essex_Town_Wenham = "Wenham";
		String Essex_Town_Rockport = "Rockport";
		String Essex_Town_Georgetown = "Georgetown";
		String Essex_Town_Peabody = "Peabody";

		String Essex_Town_Andover = "Andover";
		String Essex_Town_Beverly = "Beverly";
		String Essex_Town_Danvers = "Danvers";
		String Essex_Town_Essex = "Essex";
		String Essex_Town_Groveland = "Groveland";
		String Essex_Town_Haverhill = "Haverhill";
		String Essex_Town_Ipswitch = "Ipswitch";
		String Essex_Town_Lynn = "Lynn";
		String Essex_Town_Lynnfield = "Lynnfield";
		String Essex_Town_Manchester = "Manchester";
		String Essex_Town_Marblehead = "Marblehead";
		String Essex_Town_Merrimac = "Merrimac";
		String Essex_Town_Methuen = "Methuen";
		String Essex_Town_Middleton = "Middleton";
		String Essex_Town_Nahant = "Nahant";
		String Essex_Town_Newbury = "Newbury";
		String Essex_Town_Salem = "Salem";
		String Essex_Town_Salisbury = "Salisbury";
		String Essex_Town_Saugus = "Saugus";
		String Essex_Town_Swampscott = "Swampscott";
		String Essex_Town_Topsfield = "Topsfield";
		String Essex_Town_WestNewbury = "West Newbury";

		if (Town.equalsIgnoreCase(Essex_Town_NorthAndover)) {
			driver.get("http://epas.csc-ma.us/PublicAccess/Pages/SearchSales.aspx?town=NORTHANDOVER");
			Thread.sleep(3000);

			try {
				Essex_Washington();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Essex_Town_Boxford)) {
			driver.get("http://gis.vgsi.com/BoxfordMA/");
			Thread.sleep(3000);

			try {
				Essex_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Essex_Town_Amesbury)) {
			driver.get("http://gis.vgsi.com/Amesburyma/");
			Thread.sleep(3000);

			try {
				Essex_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Essex_Town_Gloucester)) {
			driver.get("http://gis.vgsi.com/Gloucesterma/");
			Thread.sleep(3000);

			try {
				Essex_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Essex_Town_Hamilton)) {
			driver.get("http://gis.vgsi.com/Hamiltonma/");
			Thread.sleep(3000);

			try {
				Essex_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Essex_Town_Lawrence)) {
			driver.get("http://gis.vgsi.com/Lawrencema/");
			Thread.sleep(3000);

			try {
				Essex_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Essex_Town_Newburyport)) {
			driver.get("http://gis.vgsi.com/Newburyportma/");
			Thread.sleep(3000);

			try {
				Essex_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Essex_Town_Rowley)) {
			driver.get("http://gis.vgsi.com/Rowleyma/");
			Thread.sleep(3000);

			try {
				Essex_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Essex_Town_Wenham)) {
			driver.get("https://www.axisgis.com/WenhamMa/");
			Thread.sleep(3000);

			try {
				axis_Wenham_Essex();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Essex_Town_Rockport)) {
			driver.get("http://mapsonline.net/rockportma/web_assessor/search.php#sid=f3a3532fbe814e70b30eb38b917bf87b");
			Thread.sleep(3000);

			try {
				Essex_Rockport_Otis();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Essex_Town_Georgetown)) {
			driver.get("https://mimap.mvpc.org/map/index.html?viewer=georgetown");
			Thread.sleep(10000);

			try {
				Essex_Georgetown();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Essex_Town_Peabody)) {
			driver.get("http://host.appgeo.com/PeabodyMA/");
			Thread.sleep(7000);

			try {
				Essex_Peabody();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		//////
		if (Town.equalsIgnoreCase(Essex_Town_Andover)) {
			driver.get("http://andover.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Essex_Town_Beverly)) {
			driver.get("http://beverly.patriotproperties.com/default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Essex_Town_Danvers)) {
			driver.get("http://danvers.patriotproperties.com/default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Essex_Town_Essex)) {
			driver.get("http://essex.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Essex_Town_Groveland)) {
			driver.get("http://groveland.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Essex_Town_Haverhill)) {
			driver.get("http://haverhill.patriotproperties.com/Default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Essex_Town_Ipswitch)) {
			driver.get("http://ipswich.patriotproperties.com/Default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Essex_Town_Lynn)) {
			driver.get("http://lynn.patriotproperties.com/default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Essex_Town_Lynnfield)) {
			driver.get("http://lynnfield.patriotproperties.com/Default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Essex_Town_Manchester)) {
			driver.get("http://manchester.patriotproperties.com/default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Essex_Town_Marblehead)) {
			driver.get("http://marblehead.patriotproperties.com/Default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Essex_Town_Merrimac)) {
			driver.get("http://merrimac.patriotproperties.com/default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Essex_Town_Methuen)) {
			driver.get("http://methuen.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Essex_Town_Middleton)) {
			driver.get("http://middleton.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Essex_Town_Nahant)) {
			driver.get("http://nahant.patriotproperties.com/default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Essex_Town_Newbury)) {
			driver.get("http://newbury.patriotproperties.com/default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Essex_Town_Salem)) {
			driver.get("http://salem.patriotproperties.com/default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Essex_Town_Salisbury)) {
			driver.get("http://salisbury.patriotproperties.com/default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Essex_Town_Saugus)) {
			driver.get("http://saugus.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Essex_Town_Swampscott)) {
			driver.get("http://swampscott.patriotproperties.com/Default.asp?br=exp&vr=5");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Essex_Town_Topsfield)) {
			driver.get("http://topsfield.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Essex_Town_WestNewbury)) {
			driver.get("http://westnewbury.patriotproperties.com/default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
	}

	public static void Essex_Washington() throws InterruptedException {

		// Enter Street No
		wait.until(ExpectedConditions.presenceOfElementLocated(By.name("ctl00$body$txtStreetNo"))).sendKeys(StreetNo);
		// Enter Street Name
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_body_txtStreetName"))).sendKeys(StreetName);

		// Click on Find
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_body_btnFind"))).click();
		Thread.sleep(5000);
		// Click on result link
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_body_grdSalesSummary_ctl03_lnkParcelID")))
				.click();
		Thread.sleep(5000);

		// Click on view print tab
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("104"))).click();

		driver.navigate().back();
	}

	public static void Essex_GIS() throws InterruptedException, AWTException, IOException {
		// click on Enter online database
		wait.until(ExpectedConditions.presenceOfElementLocated(By.name("ctl00$MainContent$btnEnterOnlineDatabase")))
				.click();
		Thread.sleep(3000);
		// Enter address in searchbox
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("MainContent_txtSearchAddress"))).sendKeys(ADD1);
		// click on search
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='SearchAll']/span[7]"))).click();
		Thread.sleep(3000);
		// click on Address link
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='MainContent_grdSearchResults']/tbody/tr[2]/td[1]/a")))
				.click();
		Thread.sleep(3000);

		try {
			// click on Field card to download PDF
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='MainContent_panPropCard']/span")))
					.click();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			String path = Docs_File_path + "/" + Town + ".png";
			Screenshot fpScreenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000))
					.takeScreenshot(driver);
			ImageIO.write(fpScreenshot.getImage(), "PNG", new File(path));
		}

	}

	public static void axis_Wenham_Essex() throws InterruptedException { // different case: should click multiple times
																			// on result window
		// Click on Agree
		// wait.until(ExpectedConditions.presenceOfElementLocated(By.id("closeSplash"))).click();

		try {
			Thread.sleep(5000);
			// Click on Search box
			wait.until(ExpectedConditions
					.presenceOfElementLocated(By.xpath("//*[@id='slide-in-search-handle']/div[2]/div"))).click();
			// Enter Address in Search box
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("searchInput"))).sendKeys(ADD1);

			// Click on result
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='searchGrid']/tbody/tr/td[2]")))
					.click();
			Thread.sleep(4000);

			Robot r = new Robot();
			r.keyPress(KeyEvent.VK_TAB);
			r.keyRelease(KeyEvent.VK_TAB);
			// Thread.sleep(2000);
			r.keyPress(KeyEvent.VK_TAB);
			r.keyRelease(KeyEvent.VK_TAB);
			// Thread.sleep(2000);

			try {
				// Click on result window
				wait.until(
						ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='parcelDetailTab']/ul/li[1]/a")))
						.click();
				Thread.sleep(2000);

				// Click on property card
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='Property_Card']")))
						.click();
				Thread.sleep(2000);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

				// Click on result
				wait.until(
						ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='searchGrid']/tbody/tr/td[2]")))
						.click();
				Thread.sleep(4000);

				r.keyPress(KeyEvent.VK_TAB);
				r.keyRelease(KeyEvent.VK_TAB);
				// Thread.sleep(2000);
				r.keyPress(KeyEvent.VK_TAB);
				r.keyRelease(KeyEvent.VK_TAB);
				// Thread.sleep(2000);

				// Click on result window
				wait.until(
						ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='parcelDetailTab']/ul/li[1]/a")))
						.click();
				Thread.sleep(2000);

				// Click on property card
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='Property_Card']")))
						.click();
				Thread.sleep(2000);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}

	public static void Essex_Rockport_Otis() throws InterruptedException, IOException {
		// StreetName Name
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("input_st_name"))).sendKeys(StreetName);

		// Click on Click to Search using criteria below button
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("input_st_num"))).sendKeys(StreetNo);

		// Click on Click to Search using criteria below button
		wait.until(
				ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@data-reportname='Vision Assessing Card']")))
				.click();
	}

	public static void Essex_Georgetown() throws InterruptedException {
		// Click on Find a parcel by street address link
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
				"/html/body/div[1]/div/div[1]/div/div[2]/div[1]/div[1]/div/div/div[2]/div[3]/div/div[2]/div/div/div/div[9]/a")))
				.click();
		Thread.sleep(5000);

		// Enter Street Name
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@title='Start typing name of street (All Caps)']")))
				.sendKeys(StreetName);
		Thread.sleep(5000);
		// Enter StreetNumber
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@data-gcx-form-item='ComboBox1']")))
				.sendKeys(StreetNo);
		Thread.sleep(2000);

		// Click on Search button
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//div[@class='form-btns']//button[text()='Search']"))).click();
		Thread.sleep(7000);

		// Click on result of Parcel
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
				"/html/body/div[1]/div/div[1]/div/div[2]/div[1]/div[1]/div/div/div[2]/div[5]/div/div[3]/div[2]/div[2]/div[2]/ul/li/div[1]/button/strong/div")))
				.click();
		Thread.sleep(7000);

		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("window.scrollBy(0,500)");

		// Click on view card link
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='View Card']"))).click();
		Thread.sleep(3000);
	}

	public static void Essex_Peabody() throws InterruptedException {
		// Click on agree
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[@class='btn btn-primary']"))).click();
		Thread.sleep(5000);
		// Click on next
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='tippy-1']/div/div[2]/div/div/footer/ul/li[2]/a"))).click();
		Thread.sleep(5000);
		// Click on next
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='tippy-2']/div/div[2]/div/div/footer/ul/li[3]/a"))).click();
		Thread.sleep(5000);
		// Click on next
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='tippy-3']/div/div[2]/div/div/footer/ul/li[3]/a"))).click();
		Thread.sleep(5000);
		// Click on Finish
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='tippy-4']/div/div[2]/div/div/footer/ul/li[3]/a"))).click();
		Thread.sleep(5000);

		// Click on search box
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("search-input"))).sendKeys(ADD1);
		Thread.sleep(5000);

		// Click on Address search box
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("search-field-quicksearch-displayName")))
				.sendKeys(ADD1);
		Thread.sleep(5000);
		// Click on search
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//button[@class='btn btn-primary btn-block ember-view']"))).click();
		Thread.sleep(5000);

		// Click on result
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
				"/html/body/div[3]/div/div/div[2]/div[2]/div[1]/div[2]/div/div/div/div[2]/div/div[3]/div/div/div/div[2]/div/div[3]/div/span[1]")))
				.click();
		Thread.sleep(5000);

		// Click on property record card
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
				"/html/body/div[3]/div/div/div[2]/div[2]/div[1]/div[2]/div/div/div/div[2]/div/div[2]/div/div/div/div/table/tbody/tr[3]/td/a")))
				.click();
		Thread.sleep(5000);
	}

	// franklin
	public static void franklinprocess() throws InterruptedException {
		System.out.println("**Name search details**");

		// Enter Business/Last Name
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("SearchFormEx1_ACSTextBox_LastName1")))
				.sendKeys(LName);
		Thread.sleep(3000);
		// Enter First Name
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//input[@id=\"SearchFormEx1_ACSTextBox_FirstName1\"]")))
				.sendKeys(FName);

		try {
			// Click on Search button
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("SearchFormEx1_btnSearch"))).click();
			Thread.sleep(5000);

			driver.navigate().refresh();
			Thread.sleep(1000);

			// Click on 100/page link
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("DocList1_PageView100Btn"))).click();
			Thread.sleep(2000);

			String page = driver.findElement(By.xpath("//*[@id=\"DocList1_ctl02_ctl00_LinkButtonNumber\"]")).getText();
			System.out.println(page);

			// Pagination loop
			System.out
					.println("No. of pages" + driver.findElements(By.xpath("//a[@class='PagerNumberButton']")).size());
			for (int p = 0; p <= driver.findElements(By.xpath("//a[@class='PagerNumberButton']")).size(); p++) {
				System.out.println("Page No:" + p);
				franklinDocTypeSelection();
				Thread.sleep(3000);
				driver.findElement(By.xpath("//*[text()='Next']")).click();
				Thread.sleep(3000);
			}

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println("documents are not avilable for names ");
		}

		// Property Search//

		System.out.println("**Address search details**");

		// Click on Search Criteria
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("Navigator1_SearchCriteria1_menuLabel"))).click();
		// Select Property Search
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("Navigator1_SearchCriteria1_LinkButton03")))
				.click();
		Thread.sleep(3000);

		// Enter Street No
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("SearchFormEx1_ACSTextBox_StreetNumber")))
				.sendKeys(StreetNo);
		// Enter Street Name
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("SearchFormEx1_ACSTextBox_StreetName")))
				.sendKeys(StreetName);

		try {
			// Click on Search button
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("SearchFormEx1_btnSearch"))).click();
			Thread.sleep(2000);

			// Click on 100/page link
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("DocList1_PageView100Btn"))).click();
			Thread.sleep(2000);

			// Pagination loop
			System.out
					.println("No. of pages" + driver.findElements(By.xpath("//a[@class='PagerNumberButton']")).size());
			for (int p = 0; p <= driver.findElements(By.xpath("//a[@class='PagerNumberButton']")).size(); p++) {
				System.out.println("Page No:" + p);
				franklinDocTypeSelection();
				Thread.sleep(3000);
				driver.findElement(By.xpath("//*[text()='Next']")).click();
				Thread.sleep(3000);
			}

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println("documents are not avilable for Address ");
		}
	}

	public static void printfranklinPDF() throws InterruptedException {
		// click on Print document button in popup
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("DocDetails1_PrintDocBut"))).click();
		Thread.sleep(5000);

		// Print criteria next button
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("PrintCriteriaCtrl1_ImageButton_Next"))).click();
		Thread.sleep(7000);

		// Window handling
		ArrayList<String> tab = new ArrayList<String>(driver.getWindowHandles());
		driver.switchTo().window(tab.get(1));
		Thread.sleep(5000);

		// Click here
		wait.until(ExpectedConditions.presenceOfElementLocated(
				By.xpath("//*[@id=\"PrintPreview1_WidgetContainer1\"]/table/tbody/tr/td/span[2]/a"))).click();
		Thread.sleep(7000);

		// Close current popup window
		driver.close();
		Thread.sleep(2000);

		// go back to main window
		driver.switchTo().window(tab.get(0));
		Thread.sleep(2000);
	}

	public static void franklinDocTypeSelection() throws InterruptedException {

		// Mortgage Documents

		// Printing Mortgage elements size
		System.out.println("MORTGAGE documents : " + driver.findElements(By.xpath(
				"/html/body/form/div[4]/div[27]/div[1]/div[2]/table//*[text()='MORTGAGE                                ']"))
				.size());
		Thread.sleep(3000);
		// driver.findElements(By.xpath("//a[contains(text(),'MORTGAGE')]")).size()
		for (int M = 0; M < driver.findElements(By.xpath(
				"/html/body/form/div[4]/div[27]/div[1]/div[2]/table//*[text()='MORTGAGE                                ']"))
				.size(); M++) {
			System.out.println(M);
			Thread.sleep(3000);

			try {
				// Click on only mortgage text in table
				driver.findElements(By.xpath(
						"/html/body/form/div[4]/div[27]/div[1]/div[2]/table//*[text()='MORTGAGE                                ']"))
						.get(M).click();
				Thread.sleep(5000);

				printfranklinPDF();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("Mortgage documents are not available");
				Thread.sleep(3000);
			}

		}

		// Deed documents

		System.out.println("DEED documents:" + driver.findElements(By.xpath(
				"/html/body/form/div[4]/div[27]/div[1]/div[2]/table//*[text()='DEED                                    ']"))
				.size());
		Thread.sleep(3000);

		for (int D = 0; D < driver.findElements(By.xpath(
				"/html/body/form/div[4]/div[27]/div[1]/div[2]/table//*[text()='DEED                                    ']"))
				.size(); D++) {
			System.out.println(D);
			Thread.sleep(3000);

			try {
				// Click on Deed in table
				driver.findElements(By.xpath(
						"/html/body/form/div[4]/div[27]/div[1]/div[2]/table//*[text()='DEED                                    ']"))
						.get(D).click();
				Thread.sleep(5000);

				printfranklinPDF();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("Deed documents are not available");
				Thread.sleep(3000);
			}

		}

		// LIEN Documents

		// Printing LIEN elements size
		System.out.println("LIEN documents : " + driver.findElements(By.xpath(
				"/html/body/form/div[4]/div[27]/div[1]/div[2]/table//*[text()='LIEN                                    ']"))
				.size());
		Thread.sleep(3000);
		// driver.findElements(By.xpath("//a[contains(text(),'MORTGAGE')]")).size()
		for (int LN = 0; LN < driver.findElements(By.xpath(
				"/html/body/form/div[4]/div[27]/div[1]/div[2]/table//*[text()='LIEN                                    ']"))
				.size(); LN++) {
			System.out.println(LN);
			Thread.sleep(3000);

			try {
				// Click on mortgage in table
				driver.findElements(By.xpath(
						"/html/body/form/div[4]/div[27]/div[1]/div[2]/table//*[text()='LIEN                                    ']"))
						.get(LN).click();
				Thread.sleep(5000);

				printfranklinPDF();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("LIEN documents are not available");
				Thread.sleep(3000);
			}

		}
	}

	//Franklin Township urls
	public static void Franklin_Town_URLs() throws InterruptedException {
		String Franklin_Town_Deerfield = "Deerfield";
		String Franklin_Town_Gill = "Gill";
		String Franklin_Town_Montague = "Montague";
		String Franklin_Town_Northfield = "Northfield";
		String Franklin_Town_Shelburne = "Shelburne";
		String Franklin_Town_Bernardston = "Bernardston";
		String Franklin_Town_Charlemont = "Charlemont";
		String Franklin_Town_Erving = "Erving";
		String Franklin_Town_Hawley = "Hawley";
		String Franklin_Town_Heath = "Heath";
		String Franklin_Town_Leverett = "Leverett";
		String Franklin_Town_Leyden = "Leyden";
		String Franklin_Town_Shutesbury = "Shutesbury";
		String Franklin_Town_Whately = "Whately";

		String Franklin_Town_Ashfield = "Ashfield";
		String Franklin_Town_Monroe = "Monroe";
		String Franklin_Town_Warwick = "Warwick";
		String Franklin_Town_Wendell = "Wendell";
		String Franklin_Town_Buckland = "Buckland";
		String Franklin_Town_Colrain = "Colrain";
		String Franklin_Town_Conway = "Conway";
		String Franklin_Town_Orange = "Orange";
		String Franklin_Town_Rowe = "Rowe";
		String Franklin_Town_Greenfield = "Greenfield";
		String Franklin_Town_NewSalem = "New Salem";
		String Franklin_Town_Sunderland = "Sunderland";

		if (Town.equalsIgnoreCase(Franklin_Town_Deerfield)) {
			driver.get("http://deerfield.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Franklin_Town_Gill)) {
			driver.get("http://gill.patriotproperties.com/default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Franklin_Town_Montague)) {
			driver.get("http://montague.patriotproperties.com/default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Franklin_Town_Northfield)) {
			driver.get("http://northfield.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Franklin_Town_Shelburne)) {
			driver.get("http://shelburne.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Franklin_Town_Ashfield)) {
			driver.get("http://epas.csc-ma.us/publicaccess/Pages/SearchSales.aspx?town=Ashland");
			Thread.sleep(3000);

			try {
				Franklin_Washington();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Franklin_Town_Bernardston)) {
			driver.get("http://csc-ma.us/PROPAPP/Opening.do?subAction=NewSearch&town=BernardstonPubAcc");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Franklin_Town_Monroe)) {
			driver.get("http://epas.csc-ma.us/PublicAccess/Pages/AllCommunitySearchSales.aspx");
			Thread.sleep(3000);

			try {
				Franklin_Washington();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Franklin_Town_Warwick)) {
			driver.get("http://epas.csc-ma.us/PublicAccess/Pages/SearchSales.aspx?town=WARWICK");
			Thread.sleep(3000);

			try {
				Franklin_Washington();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Franklin_Town_Wendell)) {
			driver.get("http://epas.csc-ma.us/PublicAccess/Pages/AllCommunitySearchSales.aspx");
			Thread.sleep(3000);

			try {
				Franklin_Washington();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Franklin_Town_Buckland)) {
			driver.get("https://www.axisgis.com/BucklandMA/");
			Thread.sleep(3000);

			try {
				axis_Franklin();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Franklin_Town_Charlemont)) {
			driver.get("https://www.axisgis.com/CharlemontMA/");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Franklin_Town_Colrain)) {
			driver.get("https://www.axisgis.com/colrainma/");
			Thread.sleep(3000);

			try {
				axis_Colrain_Franklin();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Franklin_Town_Conway)) {
			driver.get("https://www.axisgis.com/ConwayMA/");
			Thread.sleep(3000);

			try {
				axis_Franklin();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Franklin_Town_Orange)) {
			driver.get("https://www.axisgis.com/orangema/");
			Thread.sleep(3000);

			try {
				axis_Franklin();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Franklin_Town_Rowe)) {
			driver.get("https://www.axisgis.com/rowema/");
			Thread.sleep(3000);

			try {
				axis_Franklin();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Franklin_Town_Greenfield)) {
			driver.get("http://www.mainstreetmaps.com/MA/Greenfield/");
			Thread.sleep(3000);

			try {
				GrrenField_Franklin();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Franklin_Town_NewSalem)) {
			driver.get("http://www.mainstreetmaps.com/ma/newsalem/public.asp");
			Thread.sleep(3000);

			try {
				GrrenField_Franklin();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Franklin_Town_Erving)) {
			driver.get("http://www.erving-ma.org/town-directory/assessor");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Franklin_Town_Hawley)) {
			driver.get("http://www.townofhawley.com/hawley-assessors/");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Franklin_Town_Heath)) {
			driver.get("http://www.heathtownship.net/index.html");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Franklin_Town_Leverett)) {
			driver.get("https://leverett.ma.us/g/71/Board-of-Assessors");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Franklin_Town_Leyden)) {
			driver.get("http://www.townofleyden.com/");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Franklin_Town_Shutesbury)) {
			driver.get("http://www.shutesbury.org/assessor/");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Franklin_Town_Sunderland)) {
			driver.get("https://www.axisgis.com/SunderlandMA/");
			Thread.sleep(3000);

			try {
				axis_Franklin();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Franklin_Town_Whately)) {
			driver.get("http://www.whately.org/assessors");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

	}

	public static void Franklin_Washington() throws InterruptedException {

		// Enter Street No
		wait.until(ExpectedConditions.presenceOfElementLocated(By.name("ctl00$body$txtStreetNo"))).sendKeys(StreetNo);
		// Enter Street Name
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_body_txtStreetName"))).sendKeys(StreetName);

		// Click on Find
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_body_btnFind"))).click();
		Thread.sleep(5000);
		// Click on result link
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_body_grdSalesSummary_ctl03_lnkParcelID")))
				.click();
		Thread.sleep(5000);

		// Click on view print tab
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("104"))).click();

		driver.navigate().back();
	}

	public static void axis_Franklin() throws InterruptedException {
		try {
			Thread.sleep(5000);
			// Click on Search box
			wait.until(ExpectedConditions
					.presenceOfElementLocated(By.xpath("//*[@id='slide-in-search-handle']/div[2]/div"))).click();
			// Enter Address in Search box
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("searchInput"))).sendKeys(ADD1);

			// Click on result
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='searchGrid']/tbody/tr/td[2]")))
					.click();
			Thread.sleep(4000);

			Robot r = new Robot();
			r.keyPress(KeyEvent.VK_TAB);
			r.keyRelease(KeyEvent.VK_TAB);
			// Thread.sleep(2000);
			r.keyPress(KeyEvent.VK_TAB);
			r.keyRelease(KeyEvent.VK_TAB);
			// Thread.sleep(2000);

			try {
				// Click on result window
				wait.until(
						ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='parcelDetailTab']/ul/li[1]/a")))
						.click();
				Thread.sleep(2000);

				// Click on property card
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='CAI Property Card']")))
						.click();
				Thread.sleep(2000);
			} catch (Exception e) {

				// Click on result
				wait.until(
						ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='searchGrid']/tbody/tr/td[2]")))
						.click();
				Thread.sleep(4000);

				r.keyPress(KeyEvent.VK_TAB);
				r.keyRelease(KeyEvent.VK_TAB);
				// Thread.sleep(2000);
				r.keyPress(KeyEvent.VK_TAB);
				r.keyRelease(KeyEvent.VK_TAB);
				// Thread.sleep(2000);

				// Click on result window
				wait.until(
						ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='parcelDetailTab']/ul/li[1]/a")))
						.click();
				Thread.sleep(2000);

				// Click on property card
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='CAI Property Card']")))
						.click();
				Thread.sleep(2000);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}

	public static void axis_Colrain_Franklin() throws InterruptedException {
		try {
			Thread.sleep(5000);
			// Click on Search box
			wait.until(ExpectedConditions
					.presenceOfElementLocated(By.xpath("//*[@id='slide-in-search-handle']/div[2]/div"))).click();
			// Enter Address in Search box
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("searchInput"))).sendKeys(ADD1);

			// Click on result
			wait.until(ExpectedConditions
					.presenceOfElementLocated(By.xpath("//*[@id='searchGrid']/tbody/tr[1]/td/span/div[2]"))).click();
			Thread.sleep(4000);

			Robot r = new Robot();
			r.keyPress(KeyEvent.VK_TAB);
			r.keyRelease(KeyEvent.VK_TAB);
			// Thread.sleep(2000);
			r.keyPress(KeyEvent.VK_TAB);
			r.keyRelease(KeyEvent.VK_TAB);
			// Thread.sleep(2000);

			// Click on result window
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='parcelDetailTab']/ul/li[1]/a")))
					.click();
			Thread.sleep(2000);

			// Click on property card
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='CAI Property Card']")))
					.click();
			Thread.sleep(2000);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}

	public static void GrrenField_Franklin() throws InterruptedException {
		String Franklin_Town_NewSalem = "New Salem";
		String Franklin_Town_Greenfield = "Greenfield";

		try {
			Thread.sleep(5000);
			// Click on Search box
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("d_disc_ok"))).click();
			// Enter Address in Search box
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("s_location"))).sendKeys(ADD1);
			// Enter Address in Search box
			// wait.until(ExpectedConditions.presenceOfElementLocated(By.id("s_location"))).click();

			// Click on result
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/ul[1]/li[1]"))).click();
			Thread.sleep(2000);

			if (Town.equalsIgnoreCase(Franklin_Town_Greenfield)) {
				// Click on property card
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("b_mpc"))).click();

				// Window handling
				ArrayList<String> GrrenFieldtab = new ArrayList<String>(driver.getWindowHandles());
				driver.switchTo().window(GrrenFieldtab.get(1));
				Thread.sleep(3000);

				String path = Docs_File_path + "/" + Town + ".png";
				Screenshot fpScreenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000))
						.takeScreenshot(driver);
				ImageIO.write(fpScreenshot.getImage(), "PNG", new File(path));

				driver.close();
				driver.switchTo().window(GrrenFieldtab.get(0));

			}

			if (Town.equalsIgnoreCase(Franklin_Town_NewSalem)) {
				// Click on property card PDF
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("b_tpc"))).click();
				Thread.sleep(2000);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}

	//Hampshire Township urls
	public static void Hampshire_Town_URLs() throws InterruptedException {
		String Hampshire_Town_Belchertown = "Belchertown";
		String Hampshire_Town_Goshen = "Goshen";
		String Hampshire_Town_Worthington = "Worthington";
		String Hampshire_Town_Cummington = "Cummington";
		String Hampshire_Town_Middlefield = "Middlefield";
		String Hampshire_Town_Plainfield = "Plainfield";
		String Hampshire_Town_Westhampton = "Westhampton";
		String Hampshire_Town_Chesterfield = "Chesterfield";
		String Hampshire_Town_Southhampton = "Southhampton";
		String Hampshire_Town_Easthampton = "Easthampton";
		String Hampshire_Town_Huntington = "Huntington";
		String Hampshire_Town_Ware = "Ware";
		String Hampshire_Town_Hatfield = "Hatfield";
		String Hampshire_Town_Amherst = "Amherst";
		String Hampshire_Town_Pelham = "Pelham";
		String Hampshire_Town_Granby = "Granby";
		String Hampshire_Town_Hadley = "Hadley";
		String Hampshire_Town_Northampton = "Northampton";
		String Hampshire_Town_SouthHadley = "South Hadley";
		String Hampshire_Town_Williamsburg = "Williamsburg";

		if (Town.equalsIgnoreCase(Hampshire_Town_Belchertown)) {
			driver.get("http://belchertown.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Hampshire_Town_Goshen)) {
			driver.get("http://goshen.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Hampshire_Town_Worthington)) {
			driver.get("http://worthington.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Hampshire_Town_Cummington)) {
			driver.get("http://csc-ma.us/Cummington");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Hampshire_Town_Middlefield)) {
			driver.get("http://epas.csc-ma.us/PublicAccess/Pages/SearchSales.aspx?town=middlefield");
			Thread.sleep(3000);

			try {
				Hampshire_Washington();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Hampshire_Town_Plainfield)) {
			driver.get("http://epas.csc-ma.us/PublicAccess/Pages/SearchSales.aspx?town=PLAINFIELD");
			Thread.sleep(3000);

			try {
				Hampshire_Washington();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Hampshire_Town_Westhampton)) {
			driver.get("http://epas.csc-ma.us/PublicAccess/Pages/SearchSales.aspx?town=Westhampton");
			Thread.sleep(3000);

			try {
				Hampshire_Washington();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Hampshire_Town_Chesterfield)) {
			driver.get("http://www.assessedvalues2.com/index.aspx?jurcode=60");
			Thread.sleep(3000);

			try {
				Hampshire_assessedvalues2();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Hampshire_Town_Southhampton)) {
			driver.get("http://gis.vgsi.com/SouthamptonMA/");
			Thread.sleep(3000);

			try {
				Hampshire_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Hampshire_Town_Easthampton)) {
			driver.get("https://www.axisgis.com/EasthamptonMA/");
			Thread.sleep(5000);

			try {
				axis_Hampshire();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Hampshire_Town_Huntington)) {
			driver.get("https://www.axisgis.com/HuntingtonMA/");
			Thread.sleep(3000);

			try {
				axis_Hampshire();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Hampshire_Town_Ware)) {
			driver.get("https://www.axisgis.com/WareMA/");
			Thread.sleep(3000);

			try {
				axis_Hampshire();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Hampshire_Town_Hatfield)) {
			driver.get("http://www.mainstreetmaps.com/MA/Hatfield/#");
			Thread.sleep(3000);

			try {
				Hatfield_Hampshire();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Hampshire_Town_Amherst)) {
			driver.get("http://gis.amherstma.gov/apps/assessment/");
			Thread.sleep(5000);

			try {
				Hampshire_Amherst();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Hampshire_Town_Pelham)) {
			driver.get("http://gis.amherstma.gov/apps/pelham/");
			Thread.sleep(3000);

			try {
				Hampshire_pelham();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Hampshire_Town_Granby)) {
			driver.get("https://www.granby-ma.gov/assessor");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Hampshire_Town_Hadley)) {
			driver.get("http://www.hadleyma.org/pages/HadleyMA_Assessor/maps.pdf");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Hampshire_Town_Northampton)) {
			driver.get("http://northampton.ias-clt.com/parcel.list.php");
			Thread.sleep(3000);

			try {
				Hampshire_Northampton();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Hampshire_Town_SouthHadley)) {
			driver.get("https://www.axisgis.com/South_HadleyMA/");
			Thread.sleep(3000);

			try {
				// Click on Agree
				wait.until(ExpectedConditions
						.presenceOfElementLocated(By.xpath("//*[@id='dijit_Dialog_0']/div[1]/span[2]"))).click();
				axis_Hampshire();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Hampshire_Town_Williamsburg)) {
			driver.get("http://epas.csc-ma.us/PublicAccess/Pages/SearchSales.aspx?town=Williamsburg");
			Thread.sleep(3000);

			try {
				Hampshire_Washington();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

	}

	public static void Hampshire_Washington() throws InterruptedException {

		// Enter Street No
		wait.until(ExpectedConditions.presenceOfElementLocated(By.name("ctl00$body$txtStreetNo"))).sendKeys(StreetNo);
		// Enter Street Name
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_body_txtStreetName"))).sendKeys(StreetName);

		// Click on Find
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_body_btnFind"))).click();
		Thread.sleep(5000);
		// Click on result link
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_body_grdSalesSummary_ctl03_lnkParcelID")))
				.click();
		Thread.sleep(5000);

		// Click on view print tab
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("104"))).click();

		driver.navigate().back();
	}

	public static void Hatfield_Hampshire() throws InterruptedException {
		String Franklin_Town_NewSalem = "New Salem";
		String Franklin_Town_Greenfield = "Greenfield";

		try {
			Thread.sleep(5000);
			// Click on Search box
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("d_disc_ok"))).click();
			// Enter Address in Search box
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("s_location"))).sendKeys(ADD1);
			// Enter Address in Search box
			// wait.until(ExpectedConditions.presenceOfElementLocated(By.id("s_location"))).click();

			// Click on result
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/ul[1]/li[1]"))).click();
			Thread.sleep(2000);

			if (Town.equalsIgnoreCase(Franklin_Town_Greenfield)) {
				// Click on property card
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("b_mpc"))).click();

				// Window handling
				ArrayList<String> GrrenFieldtab = new ArrayList<String>(driver.getWindowHandles());
				driver.switchTo().window(GrrenFieldtab.get(1));
				Thread.sleep(3000);

				String path = Docs_File_path + "/" + Town + ".png";
				Screenshot fpScreenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000))
						.takeScreenshot(driver);
				ImageIO.write(fpScreenshot.getImage(), "PNG", new File(path));

				driver.close();
				driver.switchTo().window(GrrenFieldtab.get(0));

			}

			if (Town.equalsIgnoreCase(Franklin_Town_NewSalem)) {
				// Click on property card PDF
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("b_tpc"))).click();
				Thread.sleep(2000);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}

	public static void axis_Hampshire() throws InterruptedException {
		// String Address1="5 APPLE";//"410 O W BRKFLD";
		Thread.sleep(5000);

		// Click on Agree
		// wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='dijit_Dialog_0']/div[1]/span[2]"))).click();

		try {
			// Click on Search box
			wait.until(ExpectedConditions
					.presenceOfElementLocated(By.xpath("//*[@id='slide-in-search-handle']/div[2]/div"))).click();
			// Enter Address in Search box
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("searchInput"))).sendKeys(ADD1);
			// Thread.sleep(3000);

			// Click on result
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='searchGrid']/tbody/tr/td[2]")))
					.click();

			Robot r = new Robot();
			r.keyPress(KeyEvent.VK_TAB);
			r.keyRelease(KeyEvent.VK_TAB);
			Thread.sleep(2000);
			r.keyPress(KeyEvent.VK_TAB);
			r.keyRelease(KeyEvent.VK_TAB);
			Thread.sleep(2000);

			// Click on result window
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='parcelDetailTab']/ul/li[1]/a")))
					.click();

			try {
				// Click on property card
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='CAI Property Card']")))
						.click();
				Thread.sleep(2000);
			} catch (Exception e) {
				// Click on property card
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='Property_Card']")))
						.click();
				Thread.sleep(2000);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}

	public static void Hampshire_Amherst() throws InterruptedException, AWTException, IOException {

		// Enter on Street Name
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_ContentPlaceHolderPanel_ddlStreet")))
				.sendKeys(StreetName);
		Thread.sleep(2000);
		// Click on Street Number
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_ContentPlaceHolderPanel_ddlStNum")))
				.sendKeys(StreetNo);
		Thread.sleep(2000);
		// click on result
		wait.until(ExpectedConditions.presenceOfElementLocated(
				By.xpath("//*[@id='ctl00_ContentPlaceHolderPanel_GridView1']/tbody/tr[2]/td[4]/a"))).click();

	}

	public static void Hampshire_pelham() throws InterruptedException, AWTException, IOException {

		// Click on Street Name
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_ContentPlaceHolderPanel_ddlStreet")))
				.sendKeys(StreetName);
		Thread.sleep(3000);

		// Click on Street Number
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_ContentPlaceHolderPanel_ddlStNum")))
				.sendKeys(StreetNo);
		Thread.sleep(3000);

		// click on result
		wait.until(ExpectedConditions.presenceOfElementLocated(
				By.xpath("//*[@id='ctl00_ContentPlaceHolderPanel_GridView1']/tbody/tr[2]/td[5]/a"))).click();
		Thread.sleep(2000);

	}

	public static void Hampshire_Northampton() throws InterruptedException, AWTException, IOException {
		// Click on Street Name
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
				"/html/body/div/table/tbody/tr[2]/td/table/tbody/tr[2]/td/table/tbody/tr[2]/td/table/tbody/tr/td/table/tbody/tr[2]/td[4]/table/tbody/tr/td/span/select")))
				.sendKeys(StreetName);
		Thread.sleep(3000);

		// Click on Street Number
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
				"/html/body/div/table/tbody/tr[2]/td/table/tbody/tr[2]/td/table/tbody/tr[2]/td/table/tbody/tr/td/table/tbody/tr[2]/td[3]/table/tbody/tr/td/span/input")))
				.sendKeys(StreetNo);
		Thread.sleep(3000);

		// click on Search
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
				"/html/body/div/table/tbody/tr[2]/td/table/tbody/tr[2]/td/table/tbody/tr[3]/td/table/tbody/tr/td[1]/table/tbody/tr/td/span/input")))
				.click();
		Thread.sleep(3000);

		String path = Docs_File_path + "/" + Town + ".png";
		Screenshot fpScreenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000))
				.takeScreenshot(driver);
		ImageIO.write(fpScreenshot.getImage(), "PNG", new File(path));
		Thread.sleep(3000);

	}

	public static void Hampshire_GIS() throws InterruptedException, AWTException, IOException {
		// click on Enter online database
		wait.until(ExpectedConditions.presenceOfElementLocated(By.name("ctl00$MainContent$btnEnterOnlineDatabase")))
				.click();

		// Enter address in searchbox
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("MainContent_txtSearchAddress"))).sendKeys(ADD1);
		// click on search
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='SearchAll']/span[7]"))).click();

		// click on Address link
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='MainContent_grdSearchResults']/tbody/tr[2]/td[1]/a")))
				.click();

		// click on Filed card
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='MainContent_panPropCard']/span")))
				.click();

	}

	public static void Hampshire_assessedvalues2() throws InterruptedException {
		// Click on Begin Search
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_BtnSearch"))).click();
		Thread.sleep(3000);

		// Enter Street No.
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_TxtLoc1"))).sendKeys(StreetNo);
		// Enter Street No.
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_TxtStreet")))
				.sendKeys(StreetName);

		// Click on Click to Search using criteria below button
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_BtnSearch"))).click();

		try {
			// Click on Click to Search using criteria below button
			wait.until(ExpectedConditions.presenceOfElementLocated(
					By.xpath("//*[@id=\"ctl00_MainContent_Table1\"]/tbody/tr[2]/td[14]/a/img"))).click();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Assessor documents is not available for : " + Town + "&&" + e.getMessage());
		}
	}

	// Middlesex Township urls
	public static void MiddlesexTest_Town_URLs() throws InterruptedException {
		String Middlesex_Town_Ashland = "Ashland";
		String Middlesex_Town_Bedford = "Bedford";
		String Middlesex_Town_Billerica = "Billerica";
		String Middlesex_Town_Burlington = "Burlington";
		String Middlesex_Town_Dunstable = "Dunstable";
		String Middlesex_Town_Everett = "Everett";
		String Middlesex_Town_Framingham = "Framingham";
		String Middlesex_Town_Littleton = "Littleton";
		String Middlesex_Town_Malden = "Malden";
		String Middlesex_Town_Maynard = "Maynard";
		String Middlesex_Town_Melrose = "Melrose";
		String Middlesex_Town_Pepperell = "Pepperell";
		String Middlesex_Town_Reading = "Reading";
		String Middlesex_Town_Shirley = "Shirley";
		String Middlesex_Town_Stoneham = "Stoneham";
		String Middlesex_Town_Townsend = "Townsend";
		String Middlesex_Town_Tyngsborough = "Tyngsborough";
		String Middlesex_Town_Wakefield = "Wakefield";
		String Middlesex_Town_Waltham = "Waltham";
		String Middlesex_Town_Watertown = "Watertown";
		String Middlesex_Town_Westford = "Westford";
		String Middlesex_Town_Winchester = "Winchester";
		String Middlesex_Town_Ashby = "Ashby";
		String Middlesex_Town_Holliston = "Holliston";
		String Middlesex_Town_NorthReading = "North Reading";
		String Middlesex_Town_Chelmsford = "Chelmsford";
		String Middlesex_Town_Concord = "Concord";
		String Middlesex_Town_Dracut = "Dracut";
		String Middlesex_Town_Groton = "Groton";
		String Middlesex_Town_Hudson = "Hudson";
		String Middlesex_Town_Lexington = "Lexington";
		String Middlesex_Town_Medford = "Medford";
		String Middlesex_Town_Somerville = "Somerville";
		String Middlesex_Town_Stow = "Stow";
		String Middlesex_Town_Tewksbury = "Tewksbury";
		String Middlesex_Town_Wayland = "Wayland";
		String Middlesex_Town_Wilmington = "Wilmington";
		String Middlesex_Town_Woburn = "Woburn";
		String Middlesex_Town_Carlisle = "Carlisle";
		String Middlesex_Town_Lincoln = "Lincoln";
		String Middlesex_Town_Marlborough = "Marlborough";
		String Middlesex_Town_Hopkington = "Hopkington";
		String Middlesex_Town_Lowell = "Lowell";
		String Middlesex_Town_Acton = "Acton";
		String Middlesex_Town_Arlington = "Arlington";
		String Middlesex_Town_Ayer = "Ayer";
		String Middlesex_Town_Belmont = "Belmont";
		String Middlesex_Town_Boxborough = "Boxborough";
		String Middlesex_Town_Cambridge = "Cambridge";
		String Middlesex_Town_Natick = "Natick";
		String Middlesex_Town_Newton = "Newton";
		String Middlesex_Town_Sherborn = "Sherborn";
		String Middlesex_Town_Sudbury = "Sudbury";
		String Middlesex_Town_Weston = "Weston";

		if (Town.equalsIgnoreCase(Middlesex_Town_Ashland)) {
			driver.get("http://ashfield.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Bedford)) {
			driver.get("http://bedford.patriotproperties.com/default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Billerica)) {
			driver.get("http://billerica.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Burlington)) {
			driver.get("http://burlington.patriotproperties.com/default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Dunstable)) {
			driver.get("http://dunstable.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Everett)) {
			driver.get("http://everett.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Framingham)) {
			driver.get("http://framingham.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Littleton)) {
			driver.get("http://littleton.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Malden)) {
			driver.get("http://malden.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Maynard)) {
			driver.get("http://maynard.patriotproperties.com/default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Melrose)) {
			driver.get("http://melrose.patriotproperties.com/default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Pepperell)) {
			driver.get("http://pepperell.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Reading)) {
			driver.get("http://reading.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Shirley)) {
			driver.get("http://shirley.patriotproperties.com/default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Stoneham)) {
			driver.get("http://stoneham.patriotproperties.com/default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Townsend)) {
			driver.get("http://townsend.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Tyngsborough)) {
			driver.get("http://tyngsborough.patriotproperties.com/default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Wakefield)) {
			driver.get("http://wakefield.patriotproperties.com/Default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Waltham)) {
			driver.get("http://waltham.patriotproperties.com/default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Watertown)) {
			driver.get("http://watertown.patriotproperties.com/default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Westford)) {
			driver.get("http://westford.patriotproperties.com/default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Winchester)) {
			driver.get("http://winchester.patriotproperties.com/Default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Ashby)) {
			driver.get("http://epas.csc-ma.us/PublicAccess/Pages/SearchSales.aspx?town=Ashby");
			Thread.sleep(3000);

			try {
				Middlesex_epas();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Holliston)) {
			driver.get("http://epas.csc-ma.us/PublicAccess/Pages/SearchSales.aspx?town=Holliston");
			Thread.sleep(3000);

			try {
				Middlesex_epas();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_NorthReading)) {
			driver.get("http://epas.csc-ma.us/PublicAccess/Pages/SearchSales.aspx?town=NORTHREADING");
			Thread.sleep(3000);

			try {
				Middlesex_epas();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Chelmsford)) {
			driver.get("http://gis.vgsi.com/chelmsfordma/");
			Thread.sleep(3000);

			try {
				Middlesex_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Concord)) {
			driver.get("http://gis.vgsi.com/concordma/");
			Thread.sleep(3000);

			try {
				Middlesex_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Dracut)) {
			driver.get("http://gis.vgsi.com/Dracutma/");
			Thread.sleep(3000);

			try {
				Middlesex_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Groton)) {
			driver.get("http://gis.vgsi.com/grotonma/");
			Thread.sleep(3000);

			try {
				Middlesex_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Hudson)) {
			driver.get("http://gis.vgsi.com/hudsonma/");
			Thread.sleep(3000);

			try {
				Middlesex_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Lexington)) {
			driver.get("http://gis.vgsi.com/lexingtonma/");
			Thread.sleep(3000);

			try {
				Middlesex_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Medford)) {
			driver.get("http://gis.vgsi.com/Medfordma/");
			Thread.sleep(3000);

			try {
				Middlesex_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Somerville)) {
			driver.get("http://gis.vgsi.com/Somervillema/");
			Thread.sleep(3000);

			try {
				Middlesex_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Stow)) {
			driver.get("http://gis.vgsi.com/Stowma/");
			Thread.sleep(3000);

			try {
				Middlesex_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Tewksbury)) {
			driver.get("http://gis.vgsi.com/Tewksburyma/");
			Thread.sleep(3000);

			try {
				Middlesex_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Wayland)) {
			driver.get("http://gis.vgsi.com/Waylandma/");
			Thread.sleep(3000);

			try {
				Middlesex_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Wilmington)) {
			driver.get("http://gis.vgsi.com/Wilmingtonma/");
			Thread.sleep(3000);

			try {
				Middlesex_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Woburn)) {
			driver.get("http://gis.vgsi.com/Woburnma/");
			Thread.sleep(3000);

			try {
				Middlesex_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Carlisle)) {
			driver.get("http://maps.massgis.state.ma.us/map_ol/carlisle.php");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Lincoln)) {
			driver.get("https://www.axisgis.com/lincolnma/");
			Thread.sleep(7000);

			try {
				Middlesex_axis();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Marlborough)) {
			driver.get("https://www.axisgis.com/MarlboroughMA/");
			Thread.sleep(3000);

			// Click on Agree
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("closeSplash"))).click();
			try {
				Middlesex_axis();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Hopkington)) {
			driver.get(
					"http://www.mapsonline.net/hopkintonma/web_kiosk/search.php#sid=102e211eddf8bbeaca1dfe4254cfa0c6");
			Thread.sleep(3000);

			try {
				Middlesex_Hopkington();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Lowell)) {
			driver.get("http://gis.lowellma.gov/");
			Thread.sleep(3000);

			try {
				Middlesex_Lowell();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Acton)) {
			driver.get(
					"https://actonma.mapgeo.io/datasets/properties?abuttersDistance=300&latlng=42.485282%2C-71.441879");
			Thread.sleep(3000);

			try {
				Middlesex_mapgeo();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Arlington)) {
			driver.get(
					"https://www.arlingtonma.gov/departments/information-technology/geographic-information-system-gis/property-search");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Ayer)) {
			driver.get("https://mrmapper.mrpc.org/webapps/v2.15/ayer-public/");
			Thread.sleep(3000);

			try {
				Middlesex_Ayer();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Belmont)) {
			driver.get("http://belmont.jfryan.net/");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Boxborough)) {
			driver.get("https://www.axisgis.com/boxboroughma/");
			Thread.sleep(3000);

			try {
				Middlesex_axis();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Cambridge)) {
			driver.get("http://www.cambridgema.gov/fiscalaffairs/PropertySearch.cfm");
			Thread.sleep(3000);

			try {
				Middlesex_Cambridge();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Natick)) {
			driver.get(
					"https://natickma.mapgeo.io/datasets/properties?abuttersDistance=100&latlng=42.289774%2C-71.352428");
			Thread.sleep(3000);

			try {
				Middlesex_mapgeo();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Newton)) {
			driver.get(
					"https://newtonma.mapgeo.io/datasets/properties?abuttersDistance=100&latlng=42.325373%2C-71.213678");
			Thread.sleep(3000);

			try {
				Middlesex_mapgeo();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Sherborn)) {
			driver.get("https://www.mapsonline.net/sherbornma/index.html");
			Thread.sleep(3000);

			try {
				Middlesex_Sherborn();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Sudbury)) {
			driver.get("http://sudbury.ma.us/webpro/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Middlesex_Town_Weston)) {
			driver.get("https://www.mapsonline.net/westonma/trails.html");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

	}

	public static void Middlesex_GIS() throws InterruptedException, IOException {
		// click on Enter online database
		wait.until(ExpectedConditions.presenceOfElementLocated(By.name("ctl00$MainContent$btnEnterOnlineDatabase")))
				.click();

		// Enter address in searchbox
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("MainContent_txtSearchAddress"))).sendKeys(ADD1);
		// click on search
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='SearchAll']/span[7]"))).click();

		// click on Address link
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='MainContent_grdSearchResults']/tbody/tr[2]/td[1]/a")))
				.click();
		Thread.sleep(2000);

		try {
			// click on Field card to download PDF
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='MainContent_panPropCard']/span")))
					.click();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			String path = Docs_File_path + "/" + Town + ".png";
			Screenshot fpScreenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000))
					.takeScreenshot(driver);
			ImageIO.write(fpScreenshot.getImage(), "PNG", new File(path));
		}
	}

	public static void Middlesex_epas() throws InterruptedException {
		// Enter Street No
		wait.until(ExpectedConditions.presenceOfElementLocated(By.name("ctl00$body$txtStreetNo"))).sendKeys(StreetNo);
		// Enter Street Name
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_body_txtStreetName"))).sendKeys(StreetName);

		// Click on Find
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_body_btnFind"))).click();
		Thread.sleep(5000);
		// Click on result link
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_body_grdSalesSummary_ctl03_lnkParcelID")))
				.click();
		Thread.sleep(5000);

		// Click on view print tab
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("104"))).click();

		driver.navigate().back();
	}

	public static void Middlesex_Hopkington() throws InterruptedException, IOException {
		// StreetName Name
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("input_st_name"))).sendKeys(StreetName);

		// Street No
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("input_st_num"))).sendKeys(StreetNo);
		// click on property card
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("/html/body/div[2]/div[4]/div/div/div/div[3]/div/div[4]/div/a[1]")))
				.click();

		String path = Docs_File_path + "/" + Town + ".png";
		Screenshot fpScreenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000))
				.takeScreenshot(driver);
		ImageIO.write(fpScreenshot.getImage(), "PNG", new File(path));
	}

	public static void Middlesex_mapgeo() throws InterruptedException, AWTException, IOException {
		String Middlesex_Town_Newton = "Newton";
		// Click on agree
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='×']"))).click();
		Thread.sleep(5000);
		// Click on next
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='tippy-1']/div/div[2]/div/div/footer/ul/li[2]/a"))).click();
		Thread.sleep(5000);
		// Click on next
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='tippy-2']/div/div[2]/div/div/footer/ul/li[3]/a"))).click();
		Thread.sleep(5000);
		// Click on next
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='tippy-3']/div/div[2]/div/div/footer/ul/li[3]/a"))).click();
		Thread.sleep(5000);
		// Click on Finish
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='tippy-4']/div/div[2]/div/div/footer/ul/li[3]/a"))).click();
		Thread.sleep(5000);

		// Click on search box
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("search-input"))).sendKeys(ADD1);
		Thread.sleep(5000);

		// Click on Address search box
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("search-field-quicksearch-displayName")))
				.sendKeys(ADD1);
		Thread.sleep(5000);
		// Click on search
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
				"/html/body/div[3]/div/div/div[2]/div[2]/div[1]/div[2]/div/div/div/div[2]/div/div[3]/div/div/div/div[2]/div/div/form/button")))
				.click();
		Thread.sleep(5000);

		// Click on result
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
				"/html/body/div[3]/div/div/div[2]/div[2]/div[1]/div[2]/div/div/div/div[2]/div/div[3]/div/div/div/div[2]/div/div[3]")))
				.click();
		Thread.sleep(5000);

		// Click on property record card
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
				"/html/body/div[3]/div/div/div[2]/div[2]/div[1]/div[2]/div/div/div/div[2]/div/div[2]/div/div/div/div/table/tbody/tr[2]/td/a")))
				.click();
		Thread.sleep(2000);

		if (Town.equalsIgnoreCase(Middlesex_Town_Newton)) {
			// Click on view details
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
					"/html/body/div[3]/div/div/div[2]/div[2]/div[1]/div[2]/div/div/div/div[2]/div/div[2]/div/div/div/div/div[2]/a")))
					.click();
			Thread.sleep(5000);
			// click on print button
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
					"/html/body/div[3]/div/div/div[2]/div[2]/div[1]/div[2]/div/div/div/div[2]/div/div[1]/div/div[2]/div[2]/div[2]/div/div/button")))
					.click();
			Thread.sleep(5000);
			// click on print PDF button
			wait.until(ExpectedConditions
					.presenceOfElementLocated(By.xpath("/html/body/div[4]/div/div/div/div/div[1]/div/a"))).click();
			Thread.sleep(5000);

			String path = Docs_File_path + "/" + Town + ".png";
			Screenshot fpScreenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000))
					.takeScreenshot(driver);
			ImageIO.write(fpScreenshot.getImage(), "PNG", new File(path));
		}

	}

	public static void Middlesex_axis() throws InterruptedException {
		String Middlesex_Town_Lincoln = "Lincoln";
		String Middlesex_Town_Boxborough = "Boxborough";
		Thread.sleep(3000);
		// Click on Agree
		// wait.until(ExpectedConditions.presenceOfElementLocated(By.id("closeSplash"))).click();

		try {
			// Click on Search box
			wait.until(ExpectedConditions
					.presenceOfElementLocated(By.xpath("//*[@id='slide-in-search-handle']/div[2]/div"))).click();
			// Enter Address in Search box
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("searchInput"))).sendKeys(ADD1);
			// Thread.sleep(3000);

			// Click on result
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='searchGrid']/tbody/tr/td[2]")))
					.click();

			Robot r = new Robot();
			r.keyPress(KeyEvent.VK_TAB);
			r.keyRelease(KeyEvent.VK_TAB);
			Thread.sleep(2000);
			r.keyPress(KeyEvent.VK_TAB);
			r.keyRelease(KeyEvent.VK_TAB);
			Thread.sleep(2000);

			// Click on result window
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='parcelDetailTab']/ul/li[1]/a")))
					.click();

			try {
				// Click on property card
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='CAI Property Card']")))
						.click();
				Thread.sleep(2000);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (Town.equalsIgnoreCase(Middlesex_Town_Lincoln)) {
				// Click on property card
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='Property Card 1']")))
						.click();
				Thread.sleep(2000);
			}
			if (Town.equalsIgnoreCase(Middlesex_Town_Boxborough)) {
				// Click on property card
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='Patriot_Property_Card']")))
						.click();
				Thread.sleep(2000);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}

	public static void Middlesex_Sherborn() throws InterruptedException, AWTException, IOException {
		String StreetNo = "15";
		String StreetName = "APPLE";
		// Address should give same as dropdown values
		// Click on Disclaimer
		// wait.until(ExpectedConditions.presenceOfElementLocated(By.id("fancybox-close"))).click();

		// click on Find tab
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='divInfo']/div/div[1]/div"))).click();
		Thread.sleep(2000);
		// Enter on Street name
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='addr_trip_search']/table/tbody/tr/td[1]/span/input")))
				.sendKeys(StreetName);
		Thread.sleep(2000);
		// click on Street name
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/ul[1]/li/a"))).click();
		// Enter on Street No
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id=\"addr_trip_search\"]/table/tbody/tr/td[2]/span/input[1]")))
				.sendKeys(StreetNo);
		Thread.sleep(5000);
		// click on Street No
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/ul[2]/li/a"))).click();
		Thread.sleep(3000);

		// Convert web driver object to TakeScreenshot
		File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		FileUtils.copyFile(src, new File("C:\\Venky\\DownloadPDF//Sherborn.png"));
		System.out.println("screenshot taken");
	}

	public static void Middlesex_Lowell() throws InterruptedException, IOException {
		// Enter Street No
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='form1']/div[3]/table[1]/tbody/tr[1]/td[2]/input")))
				.sendKeys(StreetNo);
		// Enter Street Name
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='form1']/div[3]/table[1]/tbody/tr[2]/td[2]/select")))
				.sendKeys(StreetName);

		// Enter Street Name
		wait.until(
				ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='tblPropertyResults']/tbody/tr/td[2]/a")))
				.click();

		String path = Docs_File_path + "/" + Town + ".png";
		Screenshot fpScreenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000))
				.takeScreenshot(driver);
		ImageIO.write(fpScreenshot.getImage(), "PNG", new File(path));
		Thread.sleep(3000);
	}

	public static void Middlesex_Ayer() throws InterruptedException, AWTException, IOException {
		// click on search by address
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='Search by Address']"))).click();
		Thread.sleep(2000);
		// Enter address
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.id("widgets_Query_Widgetwidgets_Query_Widget_24_uniqName_0_0_0")))
				.sendKeys(ADD1);
		// click on Apply
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='uniqName_0_0']/div[2]"))).click();
		Thread.sleep(2000);
		// click on 3 dots
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='uniqName_0_3']/div[1]/div[1]")))
				.click();
		Thread.sleep(2000);
		// click on View in Attribute Table
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='View in Attribute Table']")))
				.click();
		Thread.sleep(5000);
		// Click on link
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='dgrid_0']/div[2]/div//*[@target='_blank']"))).click();
		Thread.sleep(5000);

		// Window handling
		ArrayList<String> Ayertab = new ArrayList<String>(driver.getWindowHandles());
		driver.switchTo().window(Ayertab.get(1));
		Thread.sleep(3000);
		System.out.println("1: " + driver.getTitle());

		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='104']/span"))).click();
		Thread.sleep(3000);

		driver.close();

		driver.switchTo().window(Ayertab.get(0));
		System.out.println("0: " + driver.getTitle());
		Thread.sleep(3000);
	}

	public static void Middlesex_Cambridge() throws InterruptedException, AWTException, IOException {

		// Enter Street no
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("body_0_content_0_txtStreetNum")))
				.sendKeys(StreetNo);
		// Enter Street name
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("body_0_content_0_txtStreetName")))
				.sendKeys(StreetName);
		// click on search
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("body_0_content_0_btnBasicSearchSubmit"))).click();

		Thread.sleep(5000);
		System.out
				.println("documents available : " + driver.findElements(By.xpath("//*[@title='Street Name']")).size());
		for (int Cambridge = 0; Cambridge < driver.findElements(By.xpath("//*[@title='Street Name']"))
				.size(); Cambridge++) {
			System.out.println("document : " + Cambridge);
			Thread.sleep(2000);

			JavascriptExecutor js = (JavascriptExecutor) driver;
			js.executeScript("window.scrollBy(0,300)");

			// Click on link
			driver.findElements(By.xpath("//*[@title='Street Name']")).get(Cambridge).click();
			Thread.sleep(3000);

			String path = Docs_File_path + "/" + Town + Cambridge + ".png";
			Screenshot fpScreenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000))
					.takeScreenshot(driver);
			ImageIO.write(fpScreenshot.getImage(), "PNG", new File(path));
			Thread.sleep(3000);

			// Click on back to search results
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("body_0_content_0_btnBackToSearchResults")))
					.click();
		}

	}

	// Nantucket Township urls
	public static void GIS_Nantucket_Town_URLs() throws InterruptedException, IOException {
		driver.get("http://gis.vgsi.com/nantucketma/");
		Thread.sleep(2000);

		// click on Enter online database
		wait.until(ExpectedConditions.presenceOfElementLocated(By.name("ctl00$MainContent$btnEnterOnlineDatabase")))
				.click();

		// Enter address in searchbox
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("MainContent_txtSearchAddress"))).sendKeys(ADD1);
		// click on search
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='SearchAll']/span[7]"))).click();

		// click on Address link
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='MainContent_grdSearchResults']/tbody/tr[2]/td[1]/a")))
				.click();
		Thread.sleep(2000);

		String path = Docs_File_path + "/" + Town + ".png";
		Screenshot fpScreenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000))
				.takeScreenshot(driver);
		ImageIO.write(fpScreenshot.getImage(), "PNG", new File(path));
	}

	// Norfolk Township urls
	public static void Norfolk_Town_URLs() throws InterruptedException {
		String Norfolk_Town_Bellingham = "Bellingham";
		String Norfolk_Town_Braintree = "Braintree";
		String Norfolk_Town_Cohasset = "Cohasset";
		String Norfolk_Town_Franklin = "Franklin";
		String Norfolk_Town_Holbrook = "Holbrook";
		String Norfolk_Town_Medfield = "Medfield";
		String Norfolk_Town_Medway = "Medway";
		String Norfolk_Town_Milton = "Milton";
		String Norfolk_Town_Stoughton = "Stoughton";

		String Norfolk_Town_Needham = "Needham";
		String Norfolk_Town_Avon = "Avon";
		String Norfolk_Town_Dedham = "Dedham";
		String Norfolk_Town_Foxborough = "Foxborough";
		String Norfolk_Town_Norfolk = "Norfolk";
		String Norfolk_Town_Norwood = "Norwood";
		String Norfolk_Town_Quincy = "Quincy";
		String Norfolk_Town_Randolph = "Randolph";
		String Norfolk_Town_Sharon = "Sharon";
		String Norfolk_Town_Walpole = "Walpole";
		String Norfolk_Town_Westwood = "Westwood";
		String Norfolk_Town_Wrentham = "Wrentham";
		String Norfolk_Town_Millis = "Millis";
		String Norfolk_Town_Plainville = "Plainville";
		String Norfolk_Town_Brookline = "Brookline";
		String Norfolk_Town_Canton = "Canton";
		String Norfolk_Town_Dover = "Dover";
		String Norfolk_Town_Wellesley = "Wellesley";
		String Norfolk_Town_Weymouth = "Weymouth";

		if (Town.equalsIgnoreCase(Norfolk_Town_Bellingham)) {
			driver.get("http://bellingham.patriotproperties.com/default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Norfolk_Town_Braintree)) {
			driver.get("http://braintree.patriotproperties.com/Default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Norfolk_Town_Cohasset)) {
			driver.get("http://cohasset.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Norfolk_Town_Franklin)) {
			driver.get("http://franklin.patriotproperties.com/default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Norfolk_Town_Holbrook)) {
			driver.get("http://holbrook.patriotproperties.com/Default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Norfolk_Town_Medfield)) {
			driver.get("http://medfield.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Norfolk_Town_Medway)) {
			driver.get("http://medway.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Norfolk_Town_Milton)) {
			driver.get("http://milton.patriotproperties.com/default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Norfolk_Town_Stoughton)) {
			driver.get("http://stoughton.patriotproperties.com/default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Norfolk_Town_Needham)) {
			driver.get("http://epas.csc-ma.us/PublicAccess/Pages/SearchSales.aspx?town=NEEDHAM");
			Thread.sleep(3000);

			try {
				Norfolk_epas();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Norfolk_Town_Avon)) {
			driver.get("http://gis.vgsi.com/avonma/");
			Thread.sleep(3000);

			try {
				Norfolk_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Norfolk_Town_Dedham)) {
			driver.get("http://gis.vgsi.com/dedhamma/");
			Thread.sleep(3000);

			try {
				Norfolk_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Norfolk_Town_Foxborough)) {
			driver.get("http://gis.vgsi.com/foxboroughma/");
			Thread.sleep(3000);

			try {
				Norfolk_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Norfolk_Town_Norfolk)) {
			driver.get("http://gis.vgsi.com/norfolkma/");
			Thread.sleep(3000);

			try {
				Norfolk_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Norfolk_Town_Norwood)) {
			driver.get("http://gis.vgsi.com/norwoodma/");
			Thread.sleep(3000);

			try {
				Norfolk_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Norfolk_Town_Quincy)) {
			driver.get("http://gis.vgsi.com/Quincyma/");
			Thread.sleep(3000);

			try {
				Norfolk_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Norfolk_Town_Randolph)) {
			driver.get("http://gis.vgsi.com/Randolphma/");
			Thread.sleep(3000);

			try {
				Norfolk_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Norfolk_Town_Sharon)) {
			driver.get("http://gis.vgsi.com/sharonma/");
			Thread.sleep(3000);

			try {
				Norfolk_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Norfolk_Town_Walpole)) {
			driver.get("http://gis.vgsi.com/Walpolema/");
			Thread.sleep(3000);

			try {
				Norfolk_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Norfolk_Town_Westwood)) {
			driver.get("http://gis.vgsi.com/Westwoodma/");
			Thread.sleep(3000);

			try {
				Norfolk_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Norfolk_Town_Wrentham)) {
			driver.get("http://gis.vgsi.com/Wrenthamma/");
			Thread.sleep(3000);

			try {
				Norfolk_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Norfolk_Town_Millis)) {
			driver.get("https://www.axisgis.com/millisma/Default.aspx");
			Thread.sleep(3000);
			// Click on Agree
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("closeSplash"))).click();
			try {
				Norfolk_axis();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Norfolk_Town_Plainville)) {
			driver.get("http://www.axisgis.com/PlainvilleMA/Default.aspx?");
			Thread.sleep(3000);
			// Click on Agree
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("closeSplash"))).click();

			try {
				Norfolk_axis();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Norfolk_Town_Brookline)) {
			driver.get("http://apps.brooklinema.gov/assessors/propertylookup.asp");
			Thread.sleep(3000);

			try {
				Norfolk_Brookline();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Norfolk_Town_Canton)) {
			driver.get("http://www.assessedvalues2.com/SearchPage.aspx?jurcode=50");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Norfolk_Town_Dover)) {
			driver.get("http://doverma.org/index.php");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Norfolk_Town_Wellesley)) {
			driver.get("http://wellesley.jfryan.net/");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Norfolk_Town_Weymouth)) {
			driver.get("https://pv.weymouth.ma.us/search");
			Thread.sleep(3000);

			try {
				Norfolk_Weymouth();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
	}

	public static void Norfolk_GIS() throws InterruptedException, IOException {
		// click on Enter online database
		wait.until(ExpectedConditions.presenceOfElementLocated(By.name("ctl00$MainContent$btnEnterOnlineDatabase")))
				.click();

		// Enter address in searchbox
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("MainContent_txtSearchAddress"))).sendKeys(ADD1);
		// click on search
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='SearchAll']/span[7]"))).click();

		// click on Address link
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='MainContent_grdSearchResults']/tbody/tr[2]/td[1]/a")))
				.click();
		Thread.sleep(2000);

		try {
			// click on Field card to download PDF
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='MainContent_panPropCard']/span")))
					.click();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			String path = Docs_File_path + "/" + Town + ".png";
			Screenshot fpScreenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000))
					.takeScreenshot(driver);
			ImageIO.write(fpScreenshot.getImage(), "PNG", new File(path));
		}
	}

	public static void Norfolk_epas() throws InterruptedException {
		// Enter Street No
		wait.until(ExpectedConditions.presenceOfElementLocated(By.name("ctl00$body$txtStreetNo"))).sendKeys(StreetNo);
		// Enter Street Name
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_body_txtStreetName"))).sendKeys(StreetName);

		// Click on Find
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_body_btnFind"))).click();
		Thread.sleep(5000);
		// Click on result link
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_body_grdSalesSummary_ctl03_lnkParcelID")))
				.click();
		Thread.sleep(5000);

		// Click on view print tab
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("104"))).click();

		driver.navigate().back();
	}

	public static void Norfolk_axis() throws InterruptedException {
		String Middlesex_Town_Lincoln = "Lincoln";
		String Middlesex_Town_Boxborough = "Boxborough";
		Thread.sleep(3000);
		// Click on Agree
		// wait.until(ExpectedConditions.presenceOfElementLocated(By.id("closeSplash"))).click();

		try {
			// Click on Search box
			wait.until(ExpectedConditions
					.presenceOfElementLocated(By.xpath("//*[@id='slide-in-search-handle']/div[2]/div"))).click();
			// Enter Address in Search box
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("searchInput"))).sendKeys(ADD1);
			// Thread.sleep(3000);

			// Click on result
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='searchGrid']/tbody/tr/td[2]")))
					.click();

			Robot r = new Robot();
			r.keyPress(KeyEvent.VK_TAB);
			r.keyRelease(KeyEvent.VK_TAB);
			Thread.sleep(2000);
			r.keyPress(KeyEvent.VK_TAB);
			r.keyRelease(KeyEvent.VK_TAB);
			Thread.sleep(2000);

			// Click on result window
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='parcelDetailTab']/ul/li[1]/a")))
					.click();

			try {
				// Click on property card
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='CAI Property Card']")))
						.click();
				Thread.sleep(2000);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (Town.equalsIgnoreCase(Middlesex_Town_Lincoln)) {
				// Click on property card
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='Property Card 1']")))
						.click();
				Thread.sleep(2000);
			}
			if (Town.equalsIgnoreCase(Middlesex_Town_Boxborough)) {
				// Click on property card
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='Patriot_Property_Card']")))
						.click();
				Thread.sleep(2000);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}

	public static void Norfolk_Assessedvalues2() throws InterruptedException {
		// Click on Begin Search
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_BtnSearch"))).click();
		Thread.sleep(3000);

		// Enter Street No.
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_TxtLoc1"))).sendKeys(StreetNo);
		// Enter Street No.
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_TxtStreet")))
				.sendKeys(StreetName);

		// Click on Click to Search using criteria below button
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_BtnSearch"))).click();

		try {
			// Click on Click to Search using criteria below button
			wait.until(ExpectedConditions.presenceOfElementLocated(
					By.xpath("//*[@id=\"ctl00_MainContent_Table1\"]/tbody/tr[2]/td[14]/a/img"))).click();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Assessor documents is not available for : " + Town + "&&" + e.getMessage());
		}
	}

	public static void Norfolk_Brookline() throws InterruptedException, AWTException, IOException {
		// Click on Street Name
		wait.until(ExpectedConditions.presenceOfElementLocated(By.name("street_1"))).sendKeys(StreetName);

		// Click on Street Number
		wait.until(ExpectedConditions.presenceOfElementLocated(By.name("address_no1"))).sendKeys(StreetNo);

		// click on submit
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@type='SUBMIT']"))).click();
		Thread.sleep(2000);
		// click on result
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@type='submit']"))).click();
		Thread.sleep(2000);

		String path = Docs_File_path + "/" + Town + ".png";
		Screenshot fpScreenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000))
				.takeScreenshot(driver);
		ImageIO.write(fpScreenshot.getImage(), "PNG", new File(path));
		Thread.sleep(3000);
	}

	public static void Norfolk_Weymouth() throws InterruptedException, AWTException, IOException {
		// Click on Street Name
		wait.until(ExpectedConditions.presenceOfElementLocated(By.name("streetName"))).sendKeys(StreetName);

		// Click on Street Number
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("tboHouseNumber"))).sendKeys(StreetNo);

		// click on search
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='property']/form/table[1]/tbody/tr[6]/td[2]/button/span")))
				.click();
		Thread.sleep(2000);
		// click on result
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='pnlSearchResults']/div/table/tbody/tr/td[4]/a"))).click();
		Thread.sleep(2000);
		// click on property link
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='pnlDataList']/div/div[3]/div/div[1]/div[3]/a"))).click();
		Thread.sleep(2000);

		// Window handling
		ArrayList<String> Weymouthtab = new ArrayList<String>(driver.getWindowHandles());
		driver.switchTo().window(Weymouthtab.get(1));
		Thread.sleep(5000);

		String path = Docs_File_path + "/" + Town + ".png";
		Screenshot fpScreenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000))
				.takeScreenshot(driver);
		ImageIO.write(fpScreenshot.getImage(), "PNG", new File(path));
		Thread.sleep(3000);

		driver.close();
		Thread.sleep(2000);
		driver.switchTo().window(Weymouthtab.get(0));
		Thread.sleep(5000);

	}

	// Plymouth Township urls
	public static void Plymouth_Town_URLs() throws InterruptedException {
		String Plymouth_Town_Hull = "Hull";
		String Plymouth_Town_Marshfield = "Marshfield";
		String Plymouth_Town_Pembroke = "Pembroke";
		String Plymouth_Town_Plymouth = "Plymouth";
		String Plymouth_Town_WestBridgewater = "West Bridgewater";
		String Plymouth_Town_Whitman = "Whitman";

		String Plymouth_Town_Bridgewater = "Bridgewater";
		String Plymouth_Town_EastBridgewater = "East Bridgewater";
		String Plymouth_Town_Halifax = "Halifax";
		String Plymouth_Town_Hanson = "Hanson";
		String Plymouth_Town_Rockland = "Rockland";
		String Plymouth_Town_Scituate = "Scituate";

		String Plymouth_Town_Duxbury = "Duxbury";
		String Plymouth_Town_Abington = "Abington";
		String Plymouth_Town_Hanover = "Hanover";
		String Plymouth_Town_Hingham = "Hingham";
		String Plymouth_Town_Kingston = "Kingston";
		String Plymouth_Town_Lakeville = "Lakeville";
		String Plymouth_Town_Marion = "Marion";
		String Plymouth_Town_Middleboro = "Middleboro";
		String Plymouth_Town_Norwell = "Norwell";
		String Plymouth_Town_Plympton = "Plympton";
		String Plymouth_Town_Rochester = "Rochester";
		String Plymouth_Town_Wareham = "Wareham";
		String Plymouth_Town_Brockton = "Brockton";
		String Plymouth_Town_Carver = "Carver";
		String Plymouth_Town_Mattapoisett = "Mattapoisett";

		if (Town.equalsIgnoreCase(Plymouth_Town_Hull)) {
			driver.get("http://hull.patriotproperties.com/default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Plymouth_Town_Marshfield)) {
			driver.get("http://marshfield.patriotproperties.com/default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Plymouth_Town_Pembroke)) {
			driver.get("http://pembroke.patriotproperties.com/default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Plymouth_Town_Plymouth)) {
			driver.get("http://plymouth.patriotproperties.com/default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Plymouth_Town_WestBridgewater)) {
			driver.get("http://westbridgewater.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Plymouth_Town_Whitman)) {
			driver.get("http://whitman.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Plymouth_Town_Bridgewater)) {
			driver.get("http://www.assessedvalues2.com/index.aspx?jurcode=42");
			Thread.sleep(3000);

			try {
				Plymouth_Assessedvalues2();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Plymouth_Town_EastBridgewater)) {
			driver.get("http://www.assessedvalues2.com/index.aspx?jurcode=83");
			Thread.sleep(3000);

			try {
				Plymouth_Assessedvalues2();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Plymouth_Town_Halifax)) {
			driver.get("http://www.assessedvalues2.com/Index.aspx?jurcode=118");
			Thread.sleep(3000);

			try {
				Plymouth_Assessedvalues2();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Plymouth_Town_Hanson)) {
			driver.get("http://www.assessedvalues2.com/index.aspx?jurcode=123");
			Thread.sleep(3000);

			try {
				Plymouth_Assessedvalues2();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Plymouth_Town_Rockland)) {
			driver.get("http://www.assessedvalues2.com/index.aspx?jurcode=251");
			Thread.sleep(3000);

			try {
				Plymouth_Assessedvalues2();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Plymouth_Town_Scituate)) {
			driver.get("http://www.assessedvalues2.com/SearchPage.aspx?jurcode=264");
			Thread.sleep(3000);

			try {
				Plymouth_Assessedvalues2();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Plymouth_Town_Duxbury)) {
			driver.get("http://gis.vgsi.com/DuxburyMA/");
			Thread.sleep(3000);

			try {
				Plymouth_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Plymouth_Town_Abington)) {
			driver.get("http://gis.vgsi.com/abingtonma/");
			Thread.sleep(3000);

			try {
				Plymouth_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Plymouth_Town_Hanover)) {
			driver.get("http://gis.vgsi.com/hanoverma/");
			Thread.sleep(3000);

			try {
				Plymouth_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Plymouth_Town_Hingham)) {
			driver.get("http://gis.vgsi.com/hinghamma/");
			Thread.sleep(3000);

			try {
				Plymouth_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Plymouth_Town_Kingston)) {
			driver.get("http://gis.vgsi.com/kingstonma/");
			Thread.sleep(3000);

			try {
				Plymouth_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Plymouth_Town_Lakeville)) {
			driver.get("http://gis.vgsi.com/lakevillema/");
			Thread.sleep(3000);

			try {
				Plymouth_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Plymouth_Town_Marion)) {
			driver.get("http://gis.vgsi.com/marionma/");
			Thread.sleep(3000);

			try {
				Plymouth_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Plymouth_Town_Middleboro)) {
			driver.get("http://gis.vgsi.com/middleboroughma/");
			Thread.sleep(3000);

			try {
				Plymouth_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Plymouth_Town_Norwell)) {
			driver.get("http://gis.vgsi.com/norwellma/");
			Thread.sleep(3000);

			try {
				Plymouth_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Plymouth_Town_Plympton)) {
			driver.get("http://gis.vgsi.com/plymptonma/");
			Thread.sleep(3000);

			try {
				Plymouth_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Plymouth_Town_Rochester)) {
			driver.get("http://gis.vgsi.com/rochesterma/");
			Thread.sleep(3000);

			try {
				Plymouth_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Plymouth_Town_Wareham)) {
			driver.get("http://gis.vgsi.com/Warehamma/");
			Thread.sleep(3000);

			try {
				Plymouth_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Plymouth_Town_Brockton)) {
			driver.get("https://hosting.tighebond.com/BrocktonMA_Public/");
			Thread.sleep(3000);

			try {
				Plymouth_Brockton();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Plymouth_Town_Carver)) {
			driver.get("http://www.carverma.org/assessors/");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Plymouth_Town_Mattapoisett)) {
			driver.get("http://www.mapsonline.net/mattapoisettma/index.html");
			Thread.sleep(3000);

			try {
				Plymouth_Mattapoisett();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
	}

	public static void Plymouth_Assessedvalues2() throws InterruptedException {
		// Click on Begin Search
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_BtnSearch"))).click();
		Thread.sleep(3000);

		// Enter Street No.
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_TxtLoc1"))).sendKeys(StreetNo);
		// Enter Street Name.
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_TxtStreet")))
				.sendKeys(StreetName);

		// Click on Click to Search using criteria below button
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_BtnSearch"))).click();

		try {
			// Click on card PDF
			wait.until(ExpectedConditions
					.presenceOfElementLocated(By.xpath("//*[@id='ctl00_MainContent_Table1']/tbody/tr[2]/td[14]/a")))
					.click();
		} catch (Exception e) {
			// Click on card PDF
			wait.until(ExpectedConditions
					.presenceOfElementLocated(By.xpath("/html/body/form/div[3]/div[2]/table/tbody/tr[2]/td[15]/a")))
					.click();
		}
	}

	public static void Plymouth_GIS() throws InterruptedException, IOException {
		// click on Enter online database
		wait.until(ExpectedConditions.presenceOfElementLocated(By.name("ctl00$MainContent$btnEnterOnlineDatabase")))
				.click();

		// Enter address in searchbox
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("MainContent_txtSearchAddress"))).sendKeys(ADD1);
		// click on search
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='SearchAll']/span[7]"))).click();

		// click on Address link
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='MainContent_grdSearchResults']/tbody/tr[2]/td[1]/a")))
				.click();
		Thread.sleep(2000);

		try {
			// click on Field card to download PDF
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='MainContent_panPropCard']/span")))
					.click();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			String path = Docs_File_path + "/" + Town + ".png";
			Screenshot fpScreenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000))
					.takeScreenshot(driver);
			ImageIO.write(fpScreenshot.getImage(), "PNG", new File(path));
		}
		String Plymouth_Town_Wareham = "Wareham";
		if (Town.equalsIgnoreCase(Plymouth_Town_Wareham)) {
			String path = Docs_File_path + "/" + Town + ".png";
			Screenshot fpScreenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000))
					.takeScreenshot(driver);
			ImageIO.write(fpScreenshot.getImage(), "PNG", new File(path));
		}
	}

	public static void Plymouth_Brockton() throws InterruptedException, AWTException, IOException {
		// Click on Street Name
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("searchinput"))).sendKeys(ADD1);

		Thread.sleep(2000);
		// click on search icon
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='SearchIcon']/span"))).click();
		Thread.sleep(2000);
		try {
			// click on property card link
			wait.until(ExpectedConditions
					.presenceOfElementLocated(By.xpath("//*[@id='infotabs']/div/div[1]/ul[1]/li[1]/a"))).click();
			Thread.sleep(5000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// click on searchresults
			wait.until(ExpectedConditions
					.presenceOfElementLocated(By.xpath("//*[@id='searchresults']/li[1]/div/div[1]/span"))).click();
			Thread.sleep(2000);
			// click on Parcel details
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='accInfo']/div[1]/h4/a/span")))
					.click();
			Thread.sleep(5000);
			// click on property card link
			wait.until(ExpectedConditions
					.presenceOfElementLocated(By.xpath("//*[@id='infotabs']/div/div[1]/ul[1]/li[1]/a"))).click();
			Thread.sleep(5000);

		}

		/*
		 * BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		 * String name = reader.readLine(); System.out.println(name);
		 */

		/*
		 * String path=Docs_File_path +"/"+Town+".png"; Screenshot fpScreenshot = new
		 * AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000)).
		 * takeScreenshot(driver); ImageIO.write(fpScreenshot.getImage(),"PNG",new
		 * File(path)); Thread.sleep(3000);
		 */

	}

	public static void Plymouth_Mattapoisett() throws InterruptedException, AWTException, IOException {
		// Click on Disclaimer
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("fancybox-close"))).click();

		// click on Find tab
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='divInfo']/div/div[1]/div"))).click();
		Thread.sleep(2000);
		// Enter on Street name
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='addr_trip_search']/table/tbody/tr/td[1]/span/input")))
				.sendKeys(StreetName);
		Thread.sleep(2000);
		// click on Street name
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/ul[1]/li/a"))).click();
		// Enter on Street No
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id=\"addr_trip_search\"]/table/tbody/tr/td[2]/span/input[1]")))
				.sendKeys(StreetNo);
		Thread.sleep(2000);
		// click on Street No
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/ul[2]/li/a"))).click();
		Thread.sleep(2000);

		// click on Property card
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
				"/html/body/div[15]/div[4]/div/div[3]/div/div/div/div[2]/div/div[6]/div[3]/div/div[3]/div/table/tbody/tr[2]/td[2]/a")))
				.click();

	}

	// Suffolk Township urls
	public static void Suffolk_Town_URLs() throws InterruptedException {
		WebDriverManager.chromedriver().setup();
		// System.setProperty("webdriver.chrome.driver",
		// "C:\\AUTOMATION\\Selenium\\Drivers\\chromedriver.exe");
		HashMap<String, Object> chromePref = new HashMap<String, Object>();
		chromePref.put("plugins.always_open_pdf_externally", true);
		chromePref.put("profile.default_content_settings.popup", 0);
		chromePref.put("download.default_directory", Docs_File_path);

		ChromeOptions options = new ChromeOptions();
		options.setExperimentalOption("prefs", chromePref);

		driver = new ChromeDriver(options);
		driver.manage().window().maximize();

		wait = new WebDriverWait(driver, 40);

		String Suffolk_Town_Revere = "Revere";
		String Suffolk_Town_Chelsea = "Chelsea";
		String Suffolk_Town_Winthrop = "Winthrop";
		String Suffolk_Town_Boston = "Boston";

		if (Town.equalsIgnoreCase(Suffolk_Town_Revere)) {
			driver.get("http://revere.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Suffolk_Town_Chelsea)) {
			driver.get("http://gis.vgsi.com/Chelseama/");
			Thread.sleep(3000);

			try {
				GIS_Suffolk();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Suffolk_Town_Winthrop)) {
			driver.get("http://gis.vgsi.com/Winthropma/");
			Thread.sleep(3000);

			try {
				GIS_Suffolk();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

		if (Town.equalsIgnoreCase(Suffolk_Town_Boston)) {
			driver.get("https://www.cityofboston.gov/assessing/search/");
			Thread.sleep(3000);

			try {
				Boston_Suffolk();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}

	}

	public static void GIS_Suffolk() throws InterruptedException, IOException {
		// click on Enter online database
		wait.until(ExpectedConditions.presenceOfElementLocated(By.name("ctl00$MainContent$btnEnterOnlineDatabase")))
				.click();

		// Enter address in searchbox
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("MainContent_txtSearchAddress"))).sendKeys(ADD1);
		// click on search
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='SearchAll']/span[7]"))).click();

		// click on Address link
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='MainContent_grdSearchResults']/tbody/tr[2]/td[1]/a")))
				.click();
		Thread.sleep(2000);

		String path = Docs_File_path + "/" + Town + ".png";
		Screenshot fpScreenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000))
				.takeScreenshot(driver);
		ImageIO.write(fpScreenshot.getImage(), "PNG", new File(path));
	}

	public static void Boston_Suffolk() throws InterruptedException, AWTException, IOException {
		// Click on Street Name
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("q"))).sendKeys(ADD1);

		Thread.sleep(2000);
		// click on search
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@type='submit']"))).click();
		Thread.sleep(2000);
		// click on Result
		wait.until(
				ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='tblSearchParcels']/tbody/tr/td[6]/a")))
				.click();
		Thread.sleep(2000);

		String path = Docs_File_path + "/" + Town + ".png";
		Screenshot fpScreenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000))
				.takeScreenshot(driver);
		ImageIO.write(fpScreenshot.getImage(), "PNG", new File(path));
		Thread.sleep(3000);

	}

	// Worcester Township urls
	public static void Worcester_Town_URLs() throws InterruptedException {
		String Worcester_Town_Charlton = "Charlton";
		String Worcester_Town_Hopedale = "Hopedale";
		String Worcester_Town_Leicester = "Leicester";
		String Worcester_Town_Milford = "Milford";
		String Worcester_Town_Millville = "Millville";
		String Worcester_Town_Northborough = "Northborough";
		String Worcester_Town_Southborough = "Southborough";
		String Worcester_Town_Westborough = "Westborough";

		String Worcester_Town_Spencer = "Spencer";
		String Worcester_Town_Bolton = "Bolton";
		String Worcester_Town_EastBrookfield = "East Brookfield";
		String Worcester_Town_Lancaster = "Lancaster";
		String Worcester_Town_Lunenburg = "Lunenburg";
		String Worcester_Town_NewBraintree = "New Braintree";
		String Worcester_Town_NorthBrookfield = "North Brookfield";
		String Worcester_Town_Royalston = "Royalston";
		String Worcester_Town_Brookfield = "Brookfield";
		String Worcester_Town_Hardwick = "Hardwick";
		String Worcester_Town_Webster = "Webster";
		String Worcester_Town_Mendon = "Mendon";
		String Worcester_Town_WestBrookfield = "West Brookfield";
		String Worcester_Town_Berlin = "Berlin";
		String Worcester_Town_Blackstone = "Blackstone";
		String Worcester_Town_Boylston = "Boylston";
		String Worcester_Town_Clinton = "Clinton";
		String Worcester_Town_Dudley = "Dudley";
		String Worcester_Town_Grafton = "Grafton";
		String Worcester_Town_Harvard = "Harvard";
		String Worcester_Town_Holden = "Holden";
		String Worcester_Town_Hubbardston = "Hubbardston";
		String Worcester_Town_Millbury = "Millbury";
		String Worcester_Town_Northbridge = "Northbridge";
		String Worcester_Town_Oxford = "Oxford";
		String Worcester_Town_Paxton = "Paxton";
		String Worcester_Town_Princeton = "Princeton";
		String Worcester_Town_Shrewsbury = "Shrewsbury";
		String Worcester_Town_Southbridge = "Southbridge";
		String Worcester_Town_Sturbridge = "Sturbridge";
		String Worcester_Town_Sutton = "Sutton";
		String Worcester_Town_Worcester = "Worcester";
		String Worcester_Town_Barre = "Barre";
		String Worcester_Town_Ashburnham = "Ashburnham";
		String Worcester_Town_Athol = "Athol";
		String Worcester_Town_Douglas = "Douglas";
		String Worcester_Town_Phillipston = "Phillipston";
		String Worcester_Town_Rutland = "Rutland";
		String Worcester_Town_Sterling = "Sterling";
		String Worcester_Town_Templeton = "Templeton";
		String Worcester_Town_Uxbridge = "Uxbridge";
		String Worcester_Town_Warren = "Warren";
		String Worcester_Town_WestBoylston = "West Boylston";
		String Worcester_Town_Winchendon = "Winchendon";
		String Worcester_Town_Fitchburg = "Fitchburg";
		String Worcester_Town_Auburn = "Auburn";
		String Worcester_Town_Gardner = "Gardner";
		String Worcester_Town_Leominster = "Leominster";
		String Worcester_Town_Oakham = "Oakham";
		String Worcester_Town_Petersham = "Petersham";
		String Worcester_Town_Upton = "Upton";
		String Worcester_Town_Westminster = "Westminster";

		if (Town.equalsIgnoreCase(Worcester_Town_Charlton)) {
			driver.get("http://charlton.patriotproperties.com/Default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Hopedale)) {
			driver.get("http://hopedale.patriotproperties.com/default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Leicester)) {
			driver.get("http://leicester.patriotproperties.com/default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Milford)) {
			driver.get("http://milford.patriotproperties.com/Default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Millville)) {
			driver.get("http://millville.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Northborough)) {
			driver.get("http://northborough.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Southborough)) {
			driver.get("http://southborough.patriotproperties.com/default.asp");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Westborough)) {
			driver.get("http://westborough.patriotproperties.com/Default.asp?br=exp&vr=6");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Spencer)) {
			driver.get(
					"https://spencerma.mapgeo.io/datasets/properties?abuttersDistance=100&latlng=42.249835%2C-71.990227");
			Thread.sleep(3000);

			try {
				Worcester_Spencer();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Bolton)) {
			driver.get("http://epas.csc-ma.us/PublicAccess/Pages/AllCommunitySearchSales.aspx");
			Thread.sleep(3000);

			try {
				Worcester_epas();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_EastBrookfield)) {
			driver.get("http://epas.csc-ma.us/PublicAccess/Pages/AllCommunitySearchSales.aspx");
			Thread.sleep(3000);

			try {
				Worcester_epas();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Lancaster)) {
			driver.get("http://epas.csc-ma.us/PublicAccess/Pages/AllCommunitySearchSales.aspx");
			Thread.sleep(3000);

			try {
				Worcester_epas();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Lunenburg)) {
			driver.get("http://epas.csc-ma.us/PublicAccess/Pages/AllCommunitySearchSales.aspx");
			Thread.sleep(3000);

			try {
				Worcester_epas();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_NewBraintree)) {
			driver.get("http://epas.csc-ma.us/PublicAccess/Pages/AllCommunitySearchSales.aspx");
			Thread.sleep(3000);

			try {
				Worcester_epas();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_NorthBrookfield)) {
			driver.get("http://epas.csc-ma.us/PublicAccess/Pages/AllCommunitySearchSales.aspx");
			Thread.sleep(3000);

			try {
				Worcester_epas();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Royalston)) {
			driver.get("http://epas.csc-ma.us/PublicAccess/Pages/AllCommunitySearchSales.aspx");
			Thread.sleep(3000);

			try {
				Worcester_epas();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Brookfield)) {
			driver.get("http://epas.csc-ma.us/publicAccess/Pages/SearchSales.aspx?town=BROOKFIELD");
			Thread.sleep(3000);

			try {
				Worcester_epas();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Hardwick)) {
			driver.get("http://epas.csc-ma.us/PublicAccess/Pages/SearchSales.aspx?town=HARDWICK");
			Thread.sleep(3000);

			try {
				Worcester_epas();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Webster)) {
			driver.get("http://www.assessedvalues2.com/Index.aspx?jurcode=316");
			Thread.sleep(3000);

			try {
				Worcester_Assessedvalues2();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Mendon)) {
			driver.get("http://www.assessedvalues2.com/SearchPage.aspx?jurcode=179");
			Thread.sleep(3000);

			try {
				Worcester_Assessedvalues2();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_WestBrookfield)) {
			driver.get("http://gis.vgsi.com/WestBrookfieldMA/");
			Thread.sleep(3000);

			try {
				Worcester_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Berlin)) {
			driver.get("http://gis.vgsi.com/berlinma/");
			Thread.sleep(3000);

			try {
				Worcester_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Blackstone)) {
			driver.get("http://gis.vgsi.com/blackstonema/");
			Thread.sleep(3000);

			try {
				Worcester_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Boylston)) {
			driver.get("http://gis.vgsi.com/boylstonma/");
			Thread.sleep(3000);

			try {
				Worcester_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Clinton)) {
			driver.get("http://gis.vgsi.com/clintonma/");
			Thread.sleep(3000);

			try {
				Worcester_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Dudley)) {
			driver.get("http://gis.vgsi.com/dudleyma/");
			Thread.sleep(3000);

			try {
				Worcester_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Grafton)) {
			driver.get("http://gis.vgsi.com/graftonma/");
			Thread.sleep(3000);

			try {
				Worcester_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Harvard)) {
			driver.get("http://gis.vgsi.com/Harvardma/");
			Thread.sleep(3000);

			try {
				Worcester_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Holden)) {
			driver.get("http://gis.vgsi.com/holdenma/");
			Thread.sleep(3000);

			try {
				Worcester_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Hubbardston)) {
			driver.get("http://gis.vgsi.com/hubbardstonma/");
			Thread.sleep(3000);

			try {
				Worcester_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Millbury)) {
			driver.get("http://gis.vgsi.com/millburyma/");
			Thread.sleep(3000);

			try {
				Worcester_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Northbridge)) {
			driver.get("http://gis.vgsi.com/northbridgema/");
			Thread.sleep(3000);

			try {
				Worcester_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Oxford)) {
			driver.get("http://gis.vgsi.com/oxfordma/");
			Thread.sleep(3000);

			try {
				Worcester_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Paxton)) {
			driver.get("http://gis.vgsi.com/paxtonma/");
			Thread.sleep(3000);

			try {
				Worcester_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Princeton)) {
			driver.get("http://gis.vgsi.com/princetonma/");
			Thread.sleep(3000);

			try {
				Worcester_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Shrewsbury)) {
			driver.get("http://gis.vgsi.com/Shrewsburyma/");
			Thread.sleep(3000);

			try {
				Worcester_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Southbridge)) {
			driver.get("http://gis.vgsi.com/Southbridgema/");
			Thread.sleep(3000);

			try {
				Worcester_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Sturbridge)) {
			driver.get("http://gis.vgsi.com/Sturbridgema/");
			Thread.sleep(3000);

			try {
				Worcester_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Sutton)) {
			driver.get("http://gis.vgsi.com/Suttonma/");
			Thread.sleep(3000);

			try {
				Worcester_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Worcester)) {
			driver.get("http://gis.vgsi.com/Worcesterma/");
			Thread.sleep(3000);

			try {
				Worcester_GIS();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Barre)) {
			driver.get("http://maps.massgis.state.ma.us/map_ol/barre.php");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Ashburnham)) {
			driver.get("https://www.axisgis.com/AshburnhamMA/");
			Thread.sleep(5000);

			try {
				Worcester_axis();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Athol)) {
			driver.get("https://www.axisgis.com/AtholMA/");
			Thread.sleep(3000);

			try {
				Worcester_axis();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Douglas)) {
			driver.get("https://www.axisgis.com/DouglasMA/");
			Thread.sleep(3000);

			try {
				Worcester_axis();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Phillipston)) {
			driver.get("https://www.axisgis.com/PHILLIPSTONMA/");
			Thread.sleep(3000);

			try {
				Worcester_axis();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Rutland)) {
			driver.get("https://www.axisgis.com/RutlandMA/Default.aspx");
			Thread.sleep(3000);
			// Click on Agree
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("closeSplash"))).click();
			Thread.sleep(3000);
			try {
				Worcester_axis();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Sterling)) {
			driver.get("https://www.axisgis.com/SterlingMA/");
			Thread.sleep(3000);

			try {
				Worcester_axis();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Templeton)) {
			driver.get("https://www.axisgis.com/templetonma/");
			Thread.sleep(3000);
			// Click on Agree
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("closeSplash"))).click();
			Thread.sleep(3000);
			try {
				Worcester_axis();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Uxbridge)) {
			driver.get("https://www.axisgis.com/UxbridgeMA/");
			Thread.sleep(3000);

			try {
				Worcester_axis();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Warren)) {
			driver.get("https://www.axisgis.com/WarrenMA/Default.aspx");
			Thread.sleep(3000);
			// Click on Agree
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("closeSplash"))).click();
			Thread.sleep(3000);
			try {
				Worcester_axis();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_WestBoylston)) {
			driver.get("https://www.axisgis.com/West_BoylstonMA/");
			Thread.sleep(3000);

			try {
				Worcester_axis();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Winchendon)) {
			driver.get("https://www.axisgis.com/WinchendonMA/");
			Thread.sleep(3000);

			try {
				Worcester_axis();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Fitchburg)) {
			driver.get("https://www.axisgis.com/fitchburgma/");
			Thread.sleep(3000);

			try {
				Worcester_axis();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Auburn)) {
			driver.get("https://www.mapsonline.net/auburnma/index.html");
			Thread.sleep(3000);

			try {
				Worcester_Auburn();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Gardner)) {
			driver.get("http://www.gardner-ma.gov/456/Property-Record-Cards");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Leominster)) {
			driver.get("https://hosting.tighebond.com/leominsterma_public/");
			Thread.sleep(3000);

			try {
				Worcester_Leominster();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Oakham)) {
			driver.get("http://maps.massgis.state.ma.us/map_ol/oakham.php");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Petersham)) {
			driver.get("https://townofpetersham.weebly.com/board-of-assessors.html");
			Thread.sleep(3000);

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Upton)) {
			driver.get("http://nereval.com/Search.aspx?town=Upton");
			Thread.sleep(3000);

			try {
				Worcester_Upton();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
		if (Town.equalsIgnoreCase(Worcester_Town_Westminster)) {
			driver.get("https://mrmapper.mrpc.org/webapps/v2.13/westminster-public/");
			Thread.sleep(3000);

			try {
				Worcester_Westminster();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(Town + ": " + driver.getTitle());
			}
		}
	}

	public static void Worcester_epas() throws InterruptedException {
		// Enter Street No
		wait.until(ExpectedConditions.presenceOfElementLocated(By.name("ctl00$body$txtStreetNo"))).sendKeys(StreetNo);
		// Enter Street Name
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_body_txtStreetName"))).sendKeys(StreetName);

		// Click on Find
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_body_btnFind"))).click();
		Thread.sleep(5000);
		// Click on result link
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_body_grdSalesSummary_ctl03_lnkParcelID")))
				.click();
		Thread.sleep(5000);

		// Click on view print tab
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("104"))).click();

		driver.navigate().back();
	}

	public static void Worcester_Assessedvalues2() throws InterruptedException {
		// Click on Begin Search
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_BtnSearch"))).click();
		Thread.sleep(3000);

		// Enter Street No.
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_TxtLoc1"))).sendKeys(StreetNo);
		// Enter Street Name.
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_TxtStreet")))
				.sendKeys(StreetName);

		// Click on Click to Search using criteria below button
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_MainContent_BtnSearch"))).click();

		try {
			// Click on card PDF
			wait.until(ExpectedConditions
					.presenceOfElementLocated(By.xpath("//*[@id='ctl00_MainContent_Table1']/tbody/tr[2]/td[14]/a")))
					.click();
		} catch (Exception e) {
			// Click on card PDF
			wait.until(ExpectedConditions
					.presenceOfElementLocated(By.xpath("/html/body/form/div[3]/div[2]/table/tbody/tr[2]/td[15]/a")))
					.click();
		}
	}

	public static void Worcester_GIS() throws InterruptedException, IOException {
		// click on Enter online database
		wait.until(ExpectedConditions.presenceOfElementLocated(By.name("ctl00$MainContent$btnEnterOnlineDatabase")))
				.click();

		// Enter address in searchbox
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("MainContent_txtSearchAddress"))).sendKeys(ADD1);
		// click on search
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='SearchAll']/span[7]"))).click();

		// click on Address link
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='MainContent_grdSearchResults']/tbody/tr[2]/td[1]/a")))
				.click();
		Thread.sleep(2000);

		try {
			// click on Field card to download PDF
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='MainContent_panPropCard']/span")))
					.click();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			String path = Docs_File_path + "/" + Town + ".png";
			Screenshot fpScreenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000))
					.takeScreenshot(driver);
			ImageIO.write(fpScreenshot.getImage(), "PNG", new File(path));
		}
		String Worcester_Town_Southbridge = "Southbridge";
		if (Town.equalsIgnoreCase(Worcester_Town_Southbridge)) {
			String path = Docs_File_path + "/" + Town + ".png";
			Screenshot fpScreenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000))
					.takeScreenshot(driver);
			ImageIO.write(fpScreenshot.getImage(), "PNG", new File(path));
		}
	}

	public static void Worcester_axis() throws InterruptedException {
		Thread.sleep(3000);
		// Click on Agree
		// wait.until(ExpectedConditions.presenceOfElementLocated(By.id("closeSplash"))).click();

		try {
			// Click on Search box
			wait.until(ExpectedConditions
					.presenceOfElementLocated(By.xpath("//*[@id='slide-in-search-handle']/div[2]/div"))).click();
			// Enter Address in Search box
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("searchInput"))).sendKeys(ADD1);
			// Thread.sleep(3000);

			// Click on result
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='searchGrid']/tbody/tr/td[2]")))
					.click();

			Robot r = new Robot();
			r.keyPress(KeyEvent.VK_TAB);
			r.keyRelease(KeyEvent.VK_TAB);
			Thread.sleep(2000);
			r.keyPress(KeyEvent.VK_TAB);
			r.keyRelease(KeyEvent.VK_TAB);
			Thread.sleep(2000);

			// Click on result window
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='parcelDetailTab']/ul/li[1]/a")))
					.click();

			try {
				// Click on property card
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='CAI Property Card']")))
						.click();
				Thread.sleep(2000);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String Worcester_Town_Rutland = "Rutland";
			String Worcester_Town_WestBoylston = "West Boylston";
			if (Town.equalsIgnoreCase(Worcester_Town_Rutland) || Town.equalsIgnoreCase(Worcester_Town_WestBoylston)) {
				// Click on property card
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='Vision_Property_Card']")))
						.click();
				Thread.sleep(2000);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}

	public static void Worcester_Spencer() throws InterruptedException, AWTException, IOException {
		// Click on agree
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='×']"))).click();
		Thread.sleep(5000);
		// Click on next
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='tippy-1']/div/div[2]/div/div/footer/ul/li[2]/a"))).click();
		Thread.sleep(5000);
		// Click on next
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='tippy-2']/div/div[2]/div/div/footer/ul/li[3]/a"))).click();
		Thread.sleep(5000);
		// Click on next
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='tippy-3']/div/div[2]/div/div/footer/ul/li[3]/a"))).click();
		Thread.sleep(5000);
		// Click on Finish
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='tippy-4']/div/div[2]/div/div/footer/ul/li[3]/a"))).click();
		Thread.sleep(5000);

		// Click on search box
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("search-input"))).sendKeys(Address1);
		Thread.sleep(5000);

		// Click on Address search box
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("search-field-quicksearch-displayName")))
				.sendKeys(Address1);
		Thread.sleep(5000);
		// Click on search
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ember133"))).click();
		Thread.sleep(5000);

		// Click on result
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
				"/html/body/div[3]/div/div/div[2]/div[2]/div[1]/div[2]/div/div/div/div[2]/div/div[3]/div/div/div/div[2]/div/div[3]")))
				.click();
		Thread.sleep(5000);

		// Click on property record card
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
				"/html/body/div[3]/div/div/div[2]/div[2]/div[1]/div[2]/div/div/div/div[2]/div/div[2]/div/div/div/div/table/tbody/tr[3]/td/a")))
				.click();
		Thread.sleep(5000);

	}

	public static void Worcester_Auburn() throws InterruptedException, AWTException, IOException {
		// Click on Disclaimer
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("fancybox-close"))).click();

		// click on Find tab
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='divInfo']/div/div[1]/div"))).click();
		Thread.sleep(2000);
		// Enter on Street name
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='addr_trip_search']/table/tbody/tr/td[1]/span/input")))
				.sendKeys(StreetName);
		Thread.sleep(2000);
		// click on Street name
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/ul[1]/li/a"))).click();
		// Enter on Street No
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='addr_trip_search']/table/tbody/tr/td[2]/span/input[1]")))
				.sendKeys(StreetNo);
		Thread.sleep(2000);
		// click on Street No
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/ul[2]/li/a"))).click();
		// Click on result
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='divQueryResultsListMulti']/div/table/tbody/tr/td[1]")))
				.click();
		try {
			// click on property card link
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='CLICK TO PRINT']"))).click();
			Thread.sleep(5000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// click on property card link
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='Open']"))).click();
		}

	}

	public static void Worcester_Leominster() throws InterruptedException, AWTException, IOException {
		// Click on Street Name
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("searchinput"))).sendKeys(ADD1);

		Thread.sleep(2000);
		// click on search icon
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='SearchIcon']/span"))).click();
		Thread.sleep(2000);
		try {
			// click on property card link
			wait.until(ExpectedConditions
					.presenceOfElementLocated(By.xpath("//*[@id='infotabs']/div/div[1]/ul[1]/li[1]/a"))).click();
			Thread.sleep(5000);
		} catch (Exception e) {
			// click on searchresults
			wait.until(ExpectedConditions
					.presenceOfElementLocated(By.xpath("//*[@id='searchresults']/li[1]/div/div[1]/span"))).click();
			Thread.sleep(2000);
			// click on Parcel details
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='accInfo']/div[1]/h4/a/span")))
					.click();
			Thread.sleep(5000);
			// click on property card link
			wait.until(ExpectedConditions
					.presenceOfElementLocated(By.xpath("//*[@id='infotabs']/div/div[1]/ul[1]/li[1]/a"))).click();
			Thread.sleep(5000);

		}
	}

	public static void Worcester_Upton() throws InterruptedException, AWTException, IOException {
		// Enter address
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("SearchProperty_tbxSearch"))).sendKeys(Address1);
		Thread.sleep(2000);
		// click on Search
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("SearchProperty_btnSearchProperty"))).click();
		// click on View
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[text()='View']"))).click();
		Thread.sleep(3000);
		// click on Search
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("Buttons_Print"))).click();

		// Window handling
		ArrayList<String> Uptontab = new ArrayList<String>(driver.getWindowHandles());
		driver.switchTo().window(Uptontab.get(1));
		Thread.sleep(5000);

		// System.out.println(driver.getTitle());

		Robot r = new Robot();
		r.keyPress(KeyEvent.VK_TAB);
		r.keyRelease(KeyEvent.VK_TAB);
		r.keyPress(KeyEvent.VK_TAB);
		r.keyRelease(KeyEvent.VK_TAB);
		r.keyPress(KeyEvent.VK_TAB);
		r.keyRelease(KeyEvent.VK_TAB);
		Thread.sleep(2000);
		r.keyPress(KeyEvent.VK_TAB);
		r.keyRelease(KeyEvent.VK_TAB);
		r.keyPress(KeyEvent.VK_TAB);
		r.keyRelease(KeyEvent.VK_TAB);
		Thread.sleep(2000);
		r.keyPress(KeyEvent.VK_TAB);
		r.keyRelease(KeyEvent.VK_TAB);
		Thread.sleep(2000);
		r.keyPress(KeyEvent.VK_ENTER);
		r.keyRelease(KeyEvent.VK_ENTER);
		Thread.sleep(2000);
		// click on Search
		// wait.until(ExpectedConditions.presenceOfElementLocated(By.id("open-button"))).click();

		driver.close();
		driver.switchTo().window(Uptontab.get(0));
	}

	public static void Worcester_Westminster() throws InterruptedException, AWTException, IOException {
		// click on check box of i agree
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='jimu_dijit_CheckBox_0']/div[1]")))
				.click();
		// click on OK
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='widgets_Splash_Widget_14']/div[2]/div[2]/div[2]/button")))
				.click();
		Thread.sleep(2000);

		// click on search by address
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[text()='Search by Address']"))).click();
		Thread.sleep(2000);
		// Enter address
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.id("widgets_Query_Widgetwidgets_Query_Widget_24_uniqName_0_0_0")))
				.sendKeys(Address1);
		// click on Apply
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='uniqName_0_0']/div[2]"))).click();

		// click on Property record card
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[text()='More info']"))).click();
		Thread.sleep(3000);

	}

	// Send zip folder to AWS S3 & send folder path as response to API
	public static void createBucket() throws Exception {

		// login AWS S3
		AWSCredentials credentials = new BasicAWSCredentials(Awsuser, Awspass);
		AmazonS3 s3client = new AmazonS3Client(credentials);

		try {
			System.out.println("Able to login AWS S3");
		} catch (Exception e) {
			System.out.println("Not able to login AWS S3");
		}

		/*
		 * // list buckets List<com.amazonaws.services.s3.model.Bucket>
		 * buckets=s3client.listBuckets(); System.out.println(" your buckets are "); for
		 * (com.amazonaws.services.s3.model.Bucket b : buckets) {
		 * System.out.println(" - " + b.getName()); }
		 */

		// Create folder
		// sendFolderToS3.createFolder(bucket, S3folderName, s3client);

		// upload folder and set it to public
		String zipFolder_name = OrderNo + "_" + CurrentDt + ".zip";

		String fileName = S3folderName + SUFFIX + zipFolder_name;

		s3client.putObject(new PutObjectRequest(bucket, fileName, new File(ZipFolder))
				.withCannedAcl(CannedAccessControlList.PublicRead));
		// delete zip folder
		// s3client.deleteObject(bucket, fileName);

		String fileName1 = S3folderName + '/' + zipFolder_name;

		// Login to super admin getting the token
		Barnstable.login();
		Thread.sleep(7000);

		System.out.println("cookies list : " + driver.manage().getCookieNamed("pippinTitleAdm"));
		String cookie = driver.manage().getCookieNamed("pippinTitleAdm").getValue();

		System.out.println("Cookie is: " + cookie);
		// Change the encoded token into decoded format
		String param2Beforedecoding = cookie;
		String param2AfterDecoding = URLDecoder.decode(param2Beforedecoding, "UTF-8");
		Object o1 = JSONValue.parse(param2AfterDecoding);
		JSONObject jsonObj = (JSONObject) o1;
		String token = (String) jsonObj.get("token");
		Object user = (Object) jsonObj.get("user");
		String user1 = user.toString();
		Object o2 = JSONValue.parse(user1);
		System.out.println("O2 is: " + o2);
		JSONObject jsonObj1 = (JSONObject) o2;
		String userID = (String) jsonObj1.get("User_ID");
		String URL = "https://absapi.pippintitle.com/";// dev API

		UUID property_id = UUID.randomUUID(); // Generates random UUID
		System.out.println("Property Id is: " + property_id);

		JSONObject files = new JSONObject();

		files.put("Name", zipFolder_name);
		files.put("Original_Name", zipFolder_name);
		files.put("Path", fileName1);
		files.put("Type", 4);

		JSONObject document = new JSONObject();
		document.put("File", files);
		document.put("Order_ID", OrderNo);
		document.put("Property_ID", property_id.toString());
		document.put("Created_By", userID);

		System.out.println("**document add order api===============>" + document);

		JSONObject POST_PARAMS611 = document;

		// Using callDocumentAPI method for calling add document API
		String uploaddocResponseMessage = Barnstable.callDocumentAPI(URL, OrderNo, cookie, document);
		if (uploaddocResponseMessage.equalsIgnoreCase("OK")) {
			BufferedReader in71 = new BufferedReader(new InputStreamReader(Barnstable.InputData));
			String inputLine211;
			StringBuffer responsecreateclient1 = new StringBuffer();
			while ((inputLine211 = in71.readLine()) != null) {
				responsecreateclient1.append(inputLine211);
			}
			in71.close();

			// String APIdocumentorder = responsecreateclient1.toString();
		}
	}

	public static void createFolder(String bucketName, String folderName, AmazonS3 client) {
		// create meta-data for your folder and set content-length to 0
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(0);
		// create empty content
		InputStream emptyContent = new ByteArrayInputStream(new byte[0]);

		String finalPath = folderName + SUFFIX;
		// create a PutObjectRequest passing the folder name suffixed by /
		PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, finalPath, emptyContent, metadata);
		// send request to S3 to create folder
		client.putObject(putObjectRequest);
	}

	public static String callDocumentAPI(String URL, String OrderNo, String cookie, JSONObject document)
			throws Exception {

		JSONObject POST_PARAMS611 = document;
		URL documentorder = new URL(URL + "orders/" + OrderNo + "/admin/documentpath");

		String param2Beforedecoding211 = cookie;
		String param2AfterDecoding211 = URLDecoder.decode(param2Beforedecoding211, "UTF-8");

		Object o411 = JSONValue.parse(param2AfterDecoding211);
		JSONObject jsonObj211 = (JSONObject) o411;
		String token211 = (String) jsonObj211.get("token");
		HttpURLConnection postConnection9 = (HttpURLConnection) documentorder.openConnection();
		postConnection9.setRequestMethod("POST");
		postConnection9.setRequestProperty("Content-Type", "application/json");
		postConnection9.setRequestProperty("Authorization", "Bearer " + token211);

		postConnection9.setDoOutput(true);
		OutputStream os111 = postConnection9.getOutputStream();
		os111.write(POST_PARAMS611.toString().getBytes());
		os111.flush();
		os111.close();
		int addDocAPIresponseCode = postConnection9.getResponseCode();
		ResponseMessage = postConnection9.getResponseMessage();
		System.out.println("Add Document API POST Response Code55 :  " + addDocAPIresponseCode);
		System.out.println("Add Document API POST Response Message55 : " + ResponseMessage);
		InputData = postConnection9.getInputStream();
		return (ResponseMessage);

	}

	public static void login() throws InterruptedException {
		String DevAdminUN = "admuser4@mailinator.com";
		String DevAdminPW = "Admins#12345";
		
		/*prop.getProperty("PPN_Url");
		String DevAdminUN=prop.getProperty("PPN_UN");
		String DevAdminPW=prop.getProperty("PPN_PW");*/

		driver.get("https://devadmin.pippintitle.com/login");
		Thread.sleep(5000);

		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("Email_Address"))).sendKeys(DevAdminUN);
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("Password"))).sendKeys(DevAdminPW);

		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("window.scrollBy(0,500)");

		Thread.sleep(5000);
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("logLogin"))).click();
	}
	
	// Split Name and Address
	public static void Splitaddress() {
		//Address1="84 Sampso venkyn";
		System.out.println("**Split Name and Address**");
		String[] split = Address1.split(" ");

		System.out.println("length is:" + split.length);
		

		//StreetName = "";
		for (int i = 1; i < split.length - 1; i++) {
			StreetName = StreetName + " " + split[i];
			StreetName = StreetName.trim();
		}
		System.out.println("StreetName is:" + StreetName.trim());

		String StreetEnd = split[split.length - 1];
		System.out.println("StreetEnd is:" + StreetEnd);

		String Avenue = "Avenue";// Av
		String Boulevard = "Boulevard";// Bv
		String Circle = "Circle";// Cr
		String Court = "Court";// Ct
		String Extension = "Extension"; // Ex
		String Heights = "Heights";// Ht
		String Lane = "Lane";// Ln
		String Place = "Place";// Pl
		String Road = "Road";// Rd
		String Run = "Run";// Rn
		String Street = "Street";// St
		String Terrace = "Terrace";// Tr
		String Drive = "Drive";
		String Way = "Way";
		if (StreetEnd.equalsIgnoreCase(Avenue)) {
			StreetEnd = "";
		} else if (StreetEnd.equalsIgnoreCase(Boulevard)) {
			StreetEnd = "";
		} else if (StreetEnd.equalsIgnoreCase(Circle)) {
			StreetEnd = "";
		} else if (StreetEnd.equalsIgnoreCase(Extension)) {
			StreetEnd = "";
		} else if (StreetEnd.equalsIgnoreCase(Heights)) {
			StreetEnd = "";
		} else if (StreetEnd.equalsIgnoreCase(Lane)) {
			StreetEnd = "";
		} else if (StreetEnd.equalsIgnoreCase(Place)) {
			StreetEnd = "";
		} else if (StreetEnd.equalsIgnoreCase(Road)) {
			StreetEnd = "";
		} else if (StreetEnd.equalsIgnoreCase(Run)) {
			StreetEnd = "";
		} else if (StreetEnd.equalsIgnoreCase(Street)) {
			StreetEnd = "";
		} else if (StreetEnd.equalsIgnoreCase(Terrace)) {
			StreetEnd = "";
		} else if (StreetEnd.equalsIgnoreCase(Drive)) {
			StreetEnd = "";
		} else if (StreetEnd.equalsIgnoreCase(Way)) {
			StreetEnd = "";
		} else {
			StreetName = StreetName + " " + StreetEnd;
			System.out.println("StreetName is:" + StreetName);
		}
		StreetEnd = "";
		StreetName = StreetName + StreetEnd;
		System.out.println("StreetName is:" + StreetName);

		// Street number
		StreetNo = split[0];
		System.out.println("StreetNo is:" + StreetNo);
		try {
			// checking valid integer using parseInt() method
			Integer.parseInt(StreetNo);
			System.out.println(StreetNo + " is a valid integer number");
			
			if(split.length==2)
			{
				StreetNo = split[0];
				System.out.println("StreetNo is:" + StreetNo);
				StreetName=split[1];
				System.out.println("StreetName is:" + StreetName);
			}

			ADD1 = StreetNo + " " + StreetName;
			System.out.println("Street No is Integer - ADD1 is:" + ADD1);
		} catch (NumberFormatException e) {
			System.out.println(StreetNo + " is not a valid integer number");
			StreetName = StreetNo + " " + StreetName;
			System.out.println("StreetName is:" + StreetName);

			ADD1 = StreetName;
			System.out.println("Street No is string - ADD1 is:" + ADD1);
		}
	}

	public static void SplitNames() {
		// Christy R. Burkhead, Brian A. Burkhead - "Todd2 Zelek2" "Genesis Title
		// Agency, LLC"

		if (BuyerName != "null") {
			System.out.println("BuyerName is: " + BuyerName);
			BuyerNameSplitComma();
		}

		else if (CoBuyerName != "null") {
			System.out.println("CoBuyerName is: " + CoBuyerName);
			CoBuyerNameSplitComma();
		}

		else if (CompanyName != "null") {
			System.out.println("CompanyName is: " + CompanyName);
			CompanyNameSplitComma();
		} else {
			FName = "null";
			System.out.println("First Name: " + FName);
			LName = "nullnull";
			System.out.println("Last Name: " + LName);
			Name = "nullnull";
			System.out.println("Name is: " + Name);
		}
	}

	public static void CompanyNameSplitComma() {
		String s[] = CompanyName.split(",");
		System.out.println("comma is available in Company Name?: " + CompanyName.contains(comma));
		if (CompanyName.contains(comma)) {
			StringTokenizer st = new StringTokenizer(CompanyName, ",");
			LName = s[0];
			System.out.println("Last Name: " + LName);
			FName = "";
			System.out.println("First Name: " + FName);
			while (st.hasMoreTokens()) {
				String s2 = st.nextToken();
				System.out.println(s2);
			}

			Name = FName + " " + LName;
			System.out.println("Name is: " + Name);

		} else {
			LName = CompanyName;
			System.out.println("Last Name: " + LName);
			FName = "";
			System.out.println("First Name: " + FName);

			Name = LName + " " + FName;
			System.out.println("Name is: " + Name);
		}
	}

	public static void BuyerNameSplitComma() {
		// String BuyerName="Christy R. Burkhead Brian A. Burkhead";//"Genesis Title
		// Agency, LLC";
		String s[] = BuyerName.split(",");
		System.out.println("comma is available in BuyerName?: " + BuyerName.contains(comma));
		if (BuyerName.contains(comma)) {
			StringTokenizer st = new StringTokenizer(BuyerName, ",");
			fullName = s[0];
			System.out.println("fullName: " + fullName);
			while (st.hasMoreTokens()) {
				String s2 = st.nextToken();
				System.out.println(s2);
			}

			String[] split = fullName.split(" ");

			FName = "";
			for (int i = 0; i < split.length - 1; i++) {
				FName = FName + " " + split[i];
				FName = FName.trim();
			}
			System.out.println("First Name is: " + FName);
			LName = split[split.length - 1];
			System.out.println("Last Name is: " + LName);

			System.out.println("length is: " + split.length);

			Name = FName + " " + LName;
			System.out.println("Name is: " + Name);
		} else {
			String[] split = BuyerName.split(" ");

			FName = "";
			for (int i = 0; i < split.length - 1; i++) {
				FName = FName + " " + split[i];
				FName = FName.trim();
			}
			System.out.println("First Name is: " + FName);
			LName = split[split.length - 1];
			System.out.println("Last Name is: " + LName);

			System.out.println("length is: " + split.length);

			Name = FName + " " + LName;
			System.out.println("Name is: " + Name);
		}
	}

	public static void CoBuyerNameSplitComma() {
		String s[] = CoBuyerName.split(",");
		System.out.println("comma is available in CoBuyerName?: " + CoBuyerName.contains(comma));
		if (CoBuyerName.contains(comma)) {
			StringTokenizer st = new StringTokenizer(CoBuyerName, ",");
			fullName = s[0];
			System.out.println("fullName: " + fullName);
			while (st.hasMoreTokens()) {
				String s2 = st.nextToken();
				System.out.println(s2);
			}

			String[] split = fullName.split(" ");

			FName = "";
			for (int i = 0; i < split.length - 1; i++) {
				FName = FName + " " + split[i];
				FName = FName.trim();
			}
			System.out.println("First Name is: " + FName);

			LName = split[split.length - 1];
			System.out.println("Last Name is: " + LName);

			System.out.println("length is: " + split.length);

			Name = FName + " " + LName;
			System.out.println("Name is: " + Name);
		} else {
			String[] split = CoBuyerName.split(" ");

			FName = "";
			for (int i = 0; i < split.length - 1; i++) {
				FName = FName + " " + split[i];
				FName = FName.trim();
			}
			System.out.println("First Name is: " + FName);
			LName = split[split.length - 1];
			System.out.println("Last Name is: " + LName);

			System.out.println("length is: " + split.length);

			Name = FName + " " + LName;
			System.out.println("Name is: " + Name);
		}
	}

	public static void CurrentDateTime() {
		Date date = new Date();
		SimpleDateFormat formatar = new SimpleDateFormat("MMddYYYYhhmm");
		CurrentDt = formatar.format(date);
		System.out.println("currentdt is: " + CurrentDt);
	}
}
