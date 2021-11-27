# April 2021 - VIDEO IGRICE

```java
public class Klijent 
{
    //salje svima svoj ID na startu

    //svi ostali klijenti mu odgovore svojim ID-em

    //svaki klijent ima fju download(String videoGameName)
    //njom klijent moze da zatrazi odredjenu video igru od bilo kog klijenta koji je poseduje
    //nebitno je koji klijent ce poslati igru, ali samo jedan sme da je posalje!

    private int id;
    private ArrayList<String> videoIgre;

    private TopicPublisher tPublisherID;
    private TopicSubscriber tSubscriberID;

    private QueueSender qSenderIdOdgovor;
    private QueueReceiver qReceiverIdOdgovor;

    private QueueSender qSenderTraziVideoIgru;
    private QueueReceiver qReceiverDobijaZahtevZaVideoIgru;

    public QueueSender qSenderVideoIgre;
    private QueueReceiver qReceiverVideoIgre;

    private TopicConnection tc;
    private TopisSession ts;

    private QueueConnection qc;
    public QueueSession qs;

    public Klijent(int id, ArrayList<String> videoIgre) 
    {
        this.id = id;
        this.videoIgre = videoIgre;

        InitialContext ictx = new InitialContext();

        TopicConnectionFastory tcf = ictx.lookup("tcfVideoIgre");
        QueueConnectionFastory qcf = ictx.lookup("qcfVideoIgre");

        Topic tID = (Topic) ictx.lookup("tID");
        Queue qIdOdgovor = (Queue) ictx.lookup("qIdOdgovor");
        Queue qZahtevZaVideoIgru = (Queue) ictx.lookup("qZahtevZaVideoIgru");
        Queue qVideoIgra = (Queue) ictx.lookup("qVideoIgra");

        ictx.close();

        tc = tcf.createConnection();
        qc = qcf.createConnection();

        ts = tc.createTopicSession(true, Session.AUTO_ACKNOWLEDGE);
        qs = qc.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);

        tPublisherID = (TopicPublisher)ts.createPublisher(tID);
        Message msg = ts.createMessage();
        msg.setIntProperty("idNovi", this.id);
        tPublisherID.send(msg);
        ts.commit();

        tSubscriberID = (TopicSubscriber)ts.createSubscriber(tID);
        qSenderIdOdgovor = (QueueSender) qs.createSender(qIdOdgovor);
        tSubscriberID.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message msg) 
            {
                System.out.println("Kreiran je novi klijent [id=" + msg.getIntProperty("idNovi") + "]");
                //kad primi id novog klijenta, klijent odgovara sa svojim id-em
                msg.setIntProperty("id", id);
                qSenderIdOdgovor.send(msg);
                qs.commit();
            }
        });

        //novi klijent cekam na odgovor klijenata koji salju svoj id
        qReceiverIdOdgovor = (QueueReceiver) qs.createReceiver(qIdOdgovor, "idNovi = " + this.id);
        //receivr prima samo za idNovi = this.id
        qReceiverIdOdgovor.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message msg) 
            {
                System.out.println("Klijent [" + mg.getIntProperty("id") + "] odgovara klijentu [" + msg.getIntProperty("idNovi") + "]");
                //to je to, ne radi nista vise
            }
        });

        //prima download pozive
        qSenderVideoIgre = (QueueSender) qs.createSender(qVideoIgra);
        for(String vi: this.videoIgre)
        {
            qReceiverDobijaZahtevZaVideoIgru = (QueueReceiver) ts.createReceiver(qZahtevZaVideoIgru, "videoIgra = '" + vi + "'");
            qReceiverDobijaZahtevZaVideoIgru.setMessageListener(new ZahtevZaVideoIgruML(this));
        }

        //primi video igru koju si trazio
        qReceiverVideoIgre = (QueueReceiver) qs.createReceiver(qVideoIgra, "idPotrazioca = " + this.id, true);
        qReceiverVideoIgre.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message msg) 
            {
                System.out.println("Igricu" + msg.getStringProperty("videoIgra") + "Vam salje klijent [" + msg.getIntProperty("idDajeIgricu") + "] :)");
                videoIgre.add(vi);
            }
        });
    }

    public void download(String videoGameName) 
    {
        //trazi video igru od bilo kog klijenta koji je poseduje
        qSenderTraziVideoIgru = (QueueSender) qs.createSender(qZahtevZaVideoIgru);
        Message msg = qs.createMessage();
        msg.setIntProperty("idPotrazioca", this.id);
        msg.setStringProperty("videoIgra", videoGameName);
        qSenderTraziVideoIgru.send(msg);
        qs.commit();
    }

    public boolean proveriDaLiImaIgricu(String igr)
    {
        if(this.videoIgre.contains(igr))
        {
            return true;
        }
        return false;
    }

    public static void main(String[] args) 
    {
        int id = GetID();//fja koja vraca jedinstven id
        ArrayList<String> videoIgre = new ArrayList<String>();

        Scanner in = new Scanner(System.in);
        System.out.println("Da li zelite da uneste video igricu u svoju listu? (DA ili NE)");
        String odg = in.nextLine();
        while(odg!="NE")
        {
            System.out.println("Unesite naziv video igrice:");
            String vi = in.nextLine();
            videoIgre.add(vi);
            System.out.println("Da li zelite da uneste video igricu u svoju listu? (DA ili NE)");
            odg = in.nextLine();
        }

        Klijent ana = new Klijent(id, videoIgre);

        System.out.println("Unesite ime igrice koju nemate, a zelite je: ");
        String newGame = in.nextLine();
        if(ana.proveriDaLiImaIgricu(newGame))
        {
            System.out.println("Vec imate tu igricu :)");
        }
        else
        {
            System.out.println("Igrica se trazi...");
            ana.download(newGame);
        }

        in.next();

        System.exit(0);
    }

    public Integer getKlijentId() 
    {
        return this.id;
    }

}
```

```java
public class ZahtevZaVideoIgruML implements MessageListener
{
    private Klijent k;

    public ZahtevZaVideoIgruML(Klijent k)
    {
        this.k = k;
    }

    public void onMessage(Message msg)
    {
        Integer idPotrazioca = msg.getIntProperty("idPotrazioca");
        String videoIgra = msg.getStringProperty("videoIgra");

        System.out.println("Klijnet [" + idPotrazioca + "] trazi video igru '" + videoIgra +"'.");

        //posto je ima, salje igricu potraziocu
        msg.setIntProperty("idDajeIgricu", this.k.getKlijentId());
        k.qSenderVideoIgre.send(msg);
        k.qs.commit();
    }
}
```
