import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Callable;

// SERVER ZA RAZMENU PORUKA 
public class User 
{
    private int id;
    private String username;
    private ChatMessageCallback callback;

    public User(int id, String username, ChatMessageCallback callback)
    {
        this.id = id;
        this.username = username;
        this.callback = callback;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public int getId()
    {
        return this.id;
    }

    public String getUsername()
    {
        return this.username;
    }

    public ChatMessageCallback getCallback()
    {
        return this.callback;
    }
}

public class ChatMessage 
{
    private User fromUser;//korisnik koji salje poruku
    private User toUser;//korisnik koji prima poruku
    private String message;//tekst poruke
    private int hour;//sati u trenutku slanja poruke
    private int minute;//minuti u trenutku slanja poruke

    public ChatMessage(User fromUser, User toUser, String message, int h, int min)
    {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.message = message;
        this.hour = h;
        this.minute = min;
    }

    public void setFromUser(User fromUser)
    {
        this.fromUser = fromUser;
    }

    public void setToUser(User toUser)
    {
        this.toUser = toUser;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public void setHour(int hour)
    {
        this.hour = hour;
    }

    public void setMinute(int min)
    {
        this.minute = min;
    }

    public User getFromUser()
    {
        return this.fromUser;
    }

    public User getToUser()
    {
        return this.toUser;
    }

    public String getMessage()
    {
        return this.message;
    }

    public int getHour()
    {
        return this.hour;
    }

    public int getMinute()
    {
        return this.minute;
    }
}

public interface ChatAppManager extends Remote
{
    public void sendChatMessage(User fromUser, User toUser, ChatMessage cmsg) throws RemoteException;
    public Vector<ChatMessage> getChatMessages(User user, int hour, int minute) throws RemoteException;
}

public class ChatAppManagerImpl extends UnicastRemoteObject implements ChatAppManager
{
    private ArrayList<ChatMessage> poslatePoruke;
    private ArrayList<User> registrovaniKorisnici;
    private ChatAppCallback callback;

    public ChatAppManagerImpl() throws RemoteException
    {
        this.poslatePoruke = new ArrayList<ChatMessage>();
        this.callback = new ChatAppCallbackImpl();
        this.registrovaniKorisni = new ArrayList<User>();
    }

    public User getUser(int id) throws RemoteException 
    {
        for(User u: this.registrovaniKorisni)
        {
            if(u.getId() == id) 
            {
                return u;
            }
        }
        return null;
    }

    public void addChatMessage(ChatMessage msg) throws RemoteException
    {
        this.poslatePoruke.add(msg);
    }

    public void addUser(User u) throws RemoteException
    {
        this.registrovaniKorisnici.add(u);
    }

    public void setPoslatePoruke(ArrayList<ChatMessage> pp) throws RemoteException
    {
        this.poslatePoruke = pp;
    }

    public void setRegistrovaniKorisni(ArrayList<User> rg) throws RemoteException
    {
        this.registrovaniKorisnici = rg;
    }

    public void setCallback(ChatAppCallback callback) throws RemoteException
    {
        this.callback = callback;
    }

    public ArrayList<ChatMessage> getPoslatePoruke() throws RemoteException
    {
        return this.poslatePoruke;
    }

    public ArrayList<User> getRegistrovaniKorisnici() throws RemoteException
    {
        return this.registrovaniKorisnici;
    }

    public ChatAppCallback getCallback() throws RemoteException
    {
        return this.callback;
    }

    public void sendChatMessage(User fromUser, User toUser, ChatMessage cmsg) throws RemoteException
    {
        //fromUser je user koji salje poruku
        //toUser prima poruku
        //cmsg sama poruka 
        //fja salje novu poruku datom korisniku
        //pri slanju poruke poziva se funkcija onChatMessage 
        //koja je definisana u callback

        if(fromUser == null || toUSer == null || cmsg == null) 
            return;

        ChatMessage message = new ChatMessage(fromUser, toUSer, cmsg.getHour(), cmsg.getMinute());
        this.poslatePoruke.add(message);
        //da li su ovo registrovani useri?
        boolean from = false;
        boolean to = false;
        for(User u: this.registrovaniKorisnici)
        {
            if(u.getId() == fromUser.getId())
            {
                //vec je registrovan
                from = true;
            }
            if(u.getId() == toUse.getId())
            {
                //vec je registrovan
                to = true;
            }
        }

        if(from == false)
        {
            //znaci nije do sada registrovan
            this.registrovaniKorisnici.addLast(fromUser);
        }
        if(to == false)
        {
            this.registrovaniKorisnici.addLast(toUser);
        }

        toUser.getCallback().onChatMessage(message);
    }

    public Vector<ChatMessage> getChatMessages(User user, int hour, int minute) throws RemoteException
    {
        //koja ce vratiti sve poruke za zadatog korisnika 
        //koje su nastale nakon prosledjenog vremena

        //naci sve poruke koje toUser imaju ovog datog 
        Vector<ChatMessage> poruke = new ArrayList<ChatMessage>();

        for(ChatMessage m: this.poslatePoruke)
        {
            if(m.getToUser().getId() == user.getUser())
            {
                //to je taj user
                //sada sve poruke do sada poslate
                if((m.getHour() < hour) || (m.getHour() == hour && m.getMinute() <= minute))
                {
                    poruke.add(m);
                }
            }
        }

        return poruke;
    }
}

public class ChatAppServer 
{
    private ChatAppManager manager;

    public ChatAppServer(String host, String port, String service)
    {
        LocateRegistry.createRegistry(Integer.parseInt(port));
        System.out.println("java rmi registry created.");
        this.manager = new ChatAppManagerImpl();
        Naming.rebind("rmi://" + host + ":" + port + "/" + service, manager);
    }

    public ChatAppManager getManager()
    {
        return this.manager;
    }

    public void sendChatMessage(User from, User to, String msg, int hour, int minute)
    {
        this.manager.sendChatMessage(from, to, new ChatMessage(from, to, msg, hour, minute));
    }

    public Vector<ChatMessage> getChatMessages(User user, int hour, int minute)
    {
        this.manager.getChatMessages(user, hour, minute);
    }

    public static void main(String[] args) 
    {
        try
        {
            ChatAppServer server = new ChatAppServer("localhost", "1099", "ChatAppService");
            Scanner s = new Scanner(System.in);

            System.out.println("Server is ready. For end - double enter.");

            s.nextLine();
            s.nextLine();
            s.close();

            System.exit(0);
        }
        catch(RemoteException e)
        {
            System.out.println(e.getMessage());
        }
    }
}

public class ChatAppClient 
{
    private ChatAppManager manager;
    private ChatAppCallback callback;

    public ChatAppClient(String host, String port, String service)
    {
        this.manager = (ChatAppManager)Naming.Lookup("rmi://" + host + ":" + port + "/" + service);
        this.callback = new ChatAppCallbackImpl();
    }

    public ChatAppManager getManager()
    {
        return manager;
    }

    public ChatAppCallback getCallback()
    {
        return callback;
    }

    public class ChatAppCallbackImpl extends UnicastRemoteObject implements ChatAppCallback
    {
        public ChatAppCallbackImpl() throws RemoteException
        {
            super();
        }

        public onChatMessage(ChatMessage msg) throws RemoteException
        {
            System.out.println("Posiljalac: " + msg.getFromUser().getUsername());
            System.out.println("Tekst: " + msg.getMessage());
        }
    }

    public static void main(String[] args) 
    {
        try
        {
            ChatAppClient client = new ChatAppClient("localhost", "1099", "ChatAppService");
            Scanner s = new Scanner(System.in);

            System.out.println("Odaberite opciju:");
            System.out.println("1 - Pogledaj sve poruke koje su do sad poslate meni");
            System.out.println("2 - Posalji novu poruku");
            System.out.println("3 - Izlaz");

            while(true) 
            {
                String opcija = s.nextLine();
                if(opcija.equals("3"))
                {
                    System.out.println("Izlaz...");
                    break;
                }
                System.out.println("Unesite svoj username:");
                String username = s.nextLine().trim();
                System.out.println("Unesit svoj id:");
                String id = s.nextLine().trim();
                User user = new User(username, id);

                if(opcija.equals("1"))
                {
                    //pribavi sve poruke do sada poslate 
                    System.out.println("Unesite trenutne sate i minute:");
                    String hour = s.nextLine().trim();
                    String minute = s.nextLine().trim();
                    //ovo sigurno moze nekom funkcijom koja vraca treutno vreme...
                    Vector<ChatMessage> poruke = client.getChatMessages(user, Integer.parseInt(hour), Integer.parseInt(minute));
                    if(poruke.size()>0)
                    {
                        for(ChatMessage m: poruke)
                        {
                            System.out.println(m);
                        }
                    }
                }
                else if(opcija.equals("2"))
                {
                    //posalji novu poruku
                    System.out.println("Unesi tekst poruke:");
                    String poruka = s.nextLine();
                    System.out.println("Unesi id primaoca: ");
                    String idd = s.nextLine().trim();
                    System.out.println("Unesi trenutne sate i minute: ");
                    String hour = s.nextLine().trim();
                    String min = s.nextLine().trim();
                    User u = client.getManager().getUser(Integer.parseInt(idd));
                    if(u == null) 
                    {
                        System.out.println("Navedeni primalac nije registrovan u sistemu!");
                    }
                    while(u==null) 
                    {
                        System.out.println("Unesite id primaoca: ");
                        idd = s.nextLine().trim();
                        u = client.getManager().getUser(Integer.parseInt(idd));
                    }

                    ChatMessage msg = new ChatMessage(user, u, poruka, horu, min);
                    client.sendMessage(msg);
                }
                else 
                {
                    System.out.println("Nevalidan unos!");
                }
            }
        }
        catch(RemoteException e) 
        {
            System.out.println("Remote error: " + e);
        }
    }
}