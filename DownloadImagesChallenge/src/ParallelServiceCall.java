import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ParallelServiceCall {
    

    public static void main(String[] args) {
        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.AddSampleRequests(2);
        var supplierStream = serviceRequest.getServiceUnorderedParallelStream();
        var ids = supplierStream.map(s ->s.get()).peek(System.out::println).filter(s -> s != null).limit(1).toList();
        System.out.println("First Success :" + (ids.isEmpty() ? "no success!" : ids.get(0)));
    }

}

class ServiceRequest {
    List<RequestSupplier> suppliers;
    
    public ServiceRequest(){
        this.suppliers = new ArrayList<>();
    }

    public void AddServiceRequest(RequestSupplier supplier){
        this.suppliers.add(supplier);
    }

    public  void AddSampleRequests(int size){
       IntStream.range(0, size).forEach(i -> AddServiceRequest(new RequestSupplier(i+1)));
    }

    public Stream<RequestSupplier> getServiceUnorderedParallelStream(){
        return this.suppliers.stream().unordered().parallel();
    }
}

class RequestSupplier implements Supplier<Integer> {
    private int id;
    public RequestSupplier(int id){
        this.id=id;
    }
    public  Integer get(){
        var result =APIService.getDriver(this.id);
        if(result== null || result == false)
            return null;
        return this.id;
    }
}

class APIService {
    final static Random random = new Random();

    public static Boolean getDriver(int id){
        try {
            Thread.sleep(random.nextInt(1000));
            boolean isSuccess = random.nextInt(10) >5 ? true : false;
            if(isSuccess){
                return true;
            }
            return null;
        } catch (InterruptedException e) {
            return null;
        }
       
    }
}
