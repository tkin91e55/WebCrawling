package com.tkk.webCrawling;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import org.jsoup.nodes.Document;
import com.tkk.webCrawling.utils.Stopwatch;

public class ConcurrencyMachine {

	// TODO: this value is got by testing, different machines should redo.
	static final int maxWorkerNumber = 15;
	private static ConcurrencyMachine instance = null;

	public static ConcurrencyMachine GetInstance() {

		if (instance == null) {
			instance = new ConcurrencyMachine();
		}

		return instance;
	}

	// TODO: runtime should be recorded just about to mass crawl, refer to
	// ECTutor

	ExecutorService executorService;
	List<Callable<Document>> requests = new ArrayList<Callable<Document>>();

	public ConcurrencyMachine() {
		executorService = Executors.newFixedThreadPool(maxWorkerNumber);
	}

	public void InvokeQueue() {

		@SuppressWarnings({ "unused" })
		List<Future<Document>> handles = new ArrayList<Future<Document>>();

		Stopwatch timer = new Stopwatch();

		try {
			// By this method, all Future runned and then this parent jump to
			// next line
			if(requests.size() < 60){
				System.out.println("[InvokingRequet] the requests size not qualified, and size is: " + requests.size());
			}
			handles = executorService.invokeAll(requests);
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("[Timer] Mass crawling elapsed time: " + timer.GetElapsedTime());

		executorService.shutdownNow();
		requests.clear();

	}

	public void RegisterQueue(Crawlee request) {
		// TODO: public function to
			requests.add(request);
	}

	public void RegisterQueue(List<Crawlee> request) {
			requests.addAll(request);
	}
}
