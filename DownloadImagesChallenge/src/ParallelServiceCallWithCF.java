import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;

public class ParallelServiceCallWithCF {
    public static void main(String[] args) {
        int numOfProcesssors = Runtime.getRuntime().availableProcessors();
        ExecutorService pool = Executors.newFixedThreadPool(numOfProcesssors);
        ServiceRequestCP requestService = new ServiceRequestCP(pool);

        int numberOfRequests = 10;
        requestService.AddSampleRequests(numberOfRequests);
        CompletableFuture<Integer>[] requests = requestService.getServiceRequests();

        LongAdder reamningRequests = new LongAdder();
        reamningRequests.add((long) numberOfRequests);

        System.out.println("Process started");
        var work = CompletableFuture.anyOf(requests);

        work.thenAccept(response -> {
            System.out.println("remaning: " + reamningRequests.sum());
            System.out.println("reponse recvied");
            if (response != null) {
                System.out.println("Success: " + response.toString());
                System.out.println("removing task: " + reamningRequests.sum());
                reamningRequests.decrement();
                System.out.println("shutting down the pool after a sucess ");
                pool.shutdownNow();

            } else {
                System.out.println("removing task: " + reamningRequests.sum());
                reamningRequests.decrement();
                System.out.println("Failed: " + response.toString());
            }
            if (reamningRequests.sum() <= 0) {
                System.out.println("shutting down the pool after all the requests have completed ");
                pool.shutdownNow();
            }

        });

        // if (pool.isShutdown()) {
        // System.out.println("Process Cleaning up");
        // pool.shutdown();
        // } else {
        // System.out.println("Process ended");
        // }

    }
}

class ServiceRequestCP {
    List<CompletableFuture<Integer>> requests;
    ExecutorService pool;

    public ServiceRequestCP(ExecutorService pool) {
        this.requests = new ArrayList<>();
        this.pool = pool;
    }

    public void AddServiceRequest(RequestSupplier supplier) {

        CompletableFuture<Integer> request = CompletableFuture.supplyAsync(supplier, this.pool);
        this.requests.add(request);
    }

    public void AddSampleRequests(int size) {
        IntStream.range(0, size).forEach(i -> AddServiceRequest(new RequestSupplier(i + 1)));
    }

    public CompletableFuture<Integer>[] getServiceRequests() {
        CompletableFuture<Integer>[] reqs = new CompletableFuture[this.requests.size()];
        for (int i = 0; i < this.requests.size(); i++) {
            reqs[i] = requests.get(i);
        }
        return reqs;
    }
}

class RequestSupplier implements Supplier<Integer> {
    private int id;

    public RequestSupplier(int id) {
        this.id = id;
    }

    public Integer get() {
        var result = APIService.getDriver(this.id);
        if (result == null || result == false)
            return null;
        return this.id;
    }
}

class APIService {
    final static Random random = new Random();

    public static Boolean getDriver(int id) {
        try {
            // System.out.println("Sending request for: " + id);
            Thread.sleep(random.nextInt(1000));
            boolean isSuccess = random.nextInt(10) > 5 ? true : false;
            if (isSuccess) {
                return true;
            }
            return null;
        } catch (InterruptedException e) {
            return null;
        }

    }
}