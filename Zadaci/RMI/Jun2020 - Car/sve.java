import java.rmi.Remote;
import java.rmi.server.UnicastRemoteObject;

/* VIRTUELNA SLUZBA ZA TRANSPORT PORUKA */
public interface Car extends Remote
{
    public void setCallback(CarCallback callback) throws RemoteException;
    public void setId(int id) throws RemoteException;
    public void setAddress(String address) throws RemoteException;
    public void setIsFree(boolean isFree) throws RemoteException;
    public int getId() throws RemoteException;
    public String getAddress() throws RemoteException;
    public boolean isFree() throws RemoteException; 
    public CarCallback getCallback() throws RemoteException;
}
public class CarImpl extends UnicastRemoteObject implements Car
{
    private int id;
    private String address;//trenutno dodeljena adresa 
    private boolean isFree;//indikacija da li je vozilo slobodno
    //ovo nam vec nagovestava da ce se menjati vremenom 
    //i da ce ovo biti remote objekat

    private CarCallback callback;
    
    public CarImpl(int id, String address, boolean isFree, CarCallback callback) throws RemoteException
    {
        this.id = id;
        this.address = address;
        this.isFree = isFree;
        this.callback = callback;
    }

    public void setCallback(CarCallback callback) throws RemoteException
    {
        this.callback = callback;
    }

    public void setAddress(String address) throws RemoteException
    {
        this.address = address;
    }

    public void setId(int id) throws RemoteException
    {
        this.id = id;
    }

    public int getId() throws RemoteException
    {
        return this.id;
    }

    public void setIsFree(boolean isFree) throws RemoteException
    {
        this.isFree = isFree;
    }

    public boolean getIsFree() throws RemoteException
    {
        return this.isFree;
    }

    public CarCallback getCallback() throws RemoteException
    {
        return this.callback;
    }
}

public interface CarManager extends Remote
{
    public boolean requestCar(String address) throws RemoteException;
}

public class CarManagerImpl extends UnicastRemoteObject implements CarManager
{
    private ArrayList<Car> vozila;
    private LinkedList<String> adreseKlijenata;//lista cekanja
    private int kapacitet;
    private CarCallback callback;

    public CarManagerImpl(int kapacitet, CarCallback callback)
    {
        this.vozila = new ArrayList<Car> ();
        this.adreseKlijenata = new LinkedList<String> ();
        this.kapacitet = kapacitet;
        this.callback = callback;
    }

    public void addVozilo(Car c) throws RemoteException
    {
        int id = this.vozila.size();
        this.vozila.addLast(new CarImpl(id, c.getAddress(), c.getIsFree()));
    }

    public void setVozila(ArrayList<String> vozila) throws RemoteException 
    {
        this.vozila = vozila;
    }

    public void setAdreseKlijenata(ArrayList<String> adrese) throws RemoteException 
    {
        this.adreseKlijenata = adrese;
    }

    public void setKapacitet(int kap) throws RemoteException
    {
        this.kapacitet = kap;
    }

    public void setCallback(CarCallback callback) throws RemoteException
    {
        this.callback = callback;
    }

    public ArraList<Car> getVozila() throws RemoteException
    {
        return this.vozila;
    }

    public LinkedList<String> getAdreseKlijenata() throws RemoteException
    {
        return this.adreseKlijenata;
    }

    public int getKapacitet() throws RemoteException
    {
        return this.kapacitet;
    }

    public CarCallback getCallback() throws RemoteException
    {
        return this.callback;
    }

    public boolean requestCar(String address) throws RemoteException
    {
        //prvom slobodnom vozilu dodeljuje adresu po Round Robin algoritmu i vraca true
        //ako ne postoji slobodno vozilo i ako postoji prazno mesto u listi cekanja 
        //onda se ubacuje adresa u listu cekanja i vraca true, a u suprotnom false
        //pri dodeljivanju adrese potrebno je pozvati metodu notifyCar(String address)
        //koja je definisana u CarCallback 

        //prvo nadjimo prvo slobodno vozilo
        //Round Robin radi tako sto uzme iz niza element, izbaci ga i ubaci na poslednje
        Car slobodno = null;
        for(Car c: this.vozila) 
        {
            if(c.getIsFree())
            {
                //ako je slobodno onda je to to vozilo 
                c.setIsFree(false);//vise nije slobodna
                slobodno = c;
                this.callback.notifyCar(slobodno);
                //round robin:
                this.vozila.remove(slobodno);
                this.vozila.addLast(slobodno);
                return true;
            }
        }

        //ako nije nadjeno ni jedno slobodno vozilo
        if(slobodno == null) 
        {
            //i ako postoji prazno mesto u listi cekanja, ubacimo adresu u listu cekanja
            if(this.adreseKlijenata.size()<this.kapacitet)
            {
                this.adreseKlijenata.addLast(address);  
                return true;   
            }
            else 
            {
                System.out.println("Nema slobodnih vozila, niti ima mesta u listi cekanja. Sorry :(");
                return false;
            }
        }
    }
}

public interface CarCallback extends Remote 
{
    public void notifyCar(String address) throws RemoteException;
}

public class CarDriverClient 
{
    private CarManager manager;
    private CarCallback callback;

    public CarDriverClient(String host, String port, String service) 
    {
        manager = (CarManager)Naming.Lookup("rmi://" + host + ":" + "port" + "/" + service);
        callback = new CarCallbackImpl();
    }

    public CarManager getManager()
    {
        return this.manager;
    }

    public CarCallback getCallback()
    {
        return this.callback;
    }

    public registrujAuto(int id, String adresa, boolean isFree) throws RemoteException
    {
        this.manager.addVozilo(new CarImpl(id, adresa, isFree));
    }

    public class CarCallbackImpl extends UnicastRemoteObject implements CarCallback
    {
        public CarCallbackImpl() throws RemoteException
        {
            super();
        }

        public void notifyCar(String address) throws RemoteException
        {
            System.out.println("Adresa na kojoj Vas ocekuje klijent je: " + address);
        }
    }

    public static void main(String[] args) 
    {
        try
        {
            CarServer driver = new CarServer("localhost", "1099", "CarService");
            Scanner s = new Scanner(System.in);

            System.out.println("Unesite svoju trenutnu adresu: ");
            String adr = s.nextLine();
            driver.registrujAuto(new CarImpl(-1, adr, true));

            s.close();
            System.exit(0);
        }
        catch(RemoteException e) 
        {
            System.out.println(e.getMessage());
        }
    }
}

public class CarUserClient 
{
    private CarManager manager;
    private CarCallback callback;

    public CarUserClient(String host, String port, String service) 
    {
        manager = (CarManager)Naming.Lookup("rmi://" + host + ":" + port + "/" + service);
        callback = new CarCallbackImpl();
    }

    public CarManager getManager()
    {
        return this.manager;
    }

    public CarCallback getCallback()
    {
        return this.callback;
    }

    public void requestCar(String address) 
    {
        this.manager.requestCar(address);
    }

    public static void main(String[] args)
    {
        try
        {
            CarUserServer user = new CarUserServer("localhost", "1099", "CarService");
            Scanner s = new Scanner(System.in);

            System.out.println("Unesite adresu na koju zelite da dodje vozilo:");
            String adr = s.nextLine();

            user.requestCar(adr);

            s.close();
            System.exit(0);
        }
        catch(RemoteException e)
        {
            System.out.println(e.getMessage());
        }
    }
}