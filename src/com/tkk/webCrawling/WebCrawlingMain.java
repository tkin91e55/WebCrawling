package com.tkk.webCrawling;

import com.tkk.webCrawling.webCrawler.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.*;

public class WebCrawlingMain {

	public static void main(String[] args) throws IOException {

		// DB flushing
		Crawlee_DB.GetInstance();

		// declared all the websites
		List<BaseCrawler> crawlers = new ArrayList<BaseCrawler>();
		crawlers.add(ECTutorCrawler.GetInstance());
		crawlers.add(L4TutorCrawler.GetInstance());
		crawlers.add(TutorGroupCrawler.GetInstance());

		// WAIT, until constructors finish and have websites get their
		// board indexes
		for (BaseCrawler crlr : crawlers) {
			List<Crawlee> crles = crlr.getCrawlees();

			synchronized (crles) {
				try {
					crlr.StartRun();
					crles.wait();

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			System.out.println(String.format("[%s crawlees] size 2: %s",crlr.toString(),crlr.getCrawlees().size()));
			ConcurrencyMachine.GetInstance().RegisterQueue(crles);
		}

		ConcurrencyMachine.GetInstance().InvokeQueue();

		// WAIT, until crawled and crawlee mature, write to same DB file
		// need to lock the log file
		Date runTime = new Date();

		for (BaseCrawler crlr : crawlers) {
			List<Crawlee> crles = crlr.getCrawlees();
		// @Problem: this is interesting, crawlees is protected inside Crawler,
		// but I can modifiy it outside here
		for (Iterator<Crawlee> crwl_iter = crles.iterator(); crwl_iter.hasNext();) {
			Crawlee crwl = crwl_iter.next();
			try {
				if (Crawlee_DB.GetInstance().LookUpFromDB(crwl, runTime)) {
					crwl_iter.remove();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		System.out.println(String.format("[%s crawlees] size before filter: %s",crlr.toString(),crlr.getCrawlees().size()));
		crlr.FilterByCritAction();
		System.out.println(String.format("[%s crawlees] size after filter: %s",crlr.toString(),crlr.getCrawlees().size()));
		}

		// WAIT, until writing DB file, write result file (this is
		// postprocessing)
		for (BaseCrawler crlr : crawlers) {
		crlr.PostProcessAction();
		}
		
		// Parsing
		FileManager filewriter = new FileManager("result.csv");
		filewriter.AppendOnNewLine(new SimpleDateFormat().format(new Date()) + " 's update:", false);
		for (Crawlee cr : ECTutorCrawler.GetInstance().getCrawlees()) {
			filewriter.AppendOnNewLine("The case index: " + cr.getCase_index());
			filewriter.AppendOnNewLine(cr.Context());
		}
		for (Crawlee cr : L4TutorCrawler.GetInstance().getCrawlees()) {
			filewriter.AppendOnNewLine("The case index: " + cr.getCase_index());
			filewriter.AppendOnNewLine(cr.Context());
		}
		for (Crawlee cr : TutorGroupCrawler.GetInstance().getCrawlees()) {
			filewriter.AppendOnNewLine(cr.Context());
		}
		filewriter.Close();

		System.out.println("Program main runned to LAST line!");

	}

}
