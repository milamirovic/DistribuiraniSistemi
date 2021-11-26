# Automatizvoana proizvodnja - zadatak

Imamo *n* klijenata gde svaki ima svoju listu proizvoda/usluga koje obavlja i svaki od kiljenata može od ostalih da zatraži pomoć, sa tim da kada zatraži pomoć može da mu pomogne samo 1 klijent može da mu pomogne, a ne svi klijenti koji obavljaju isti posao.
Obavljaju neku min komunikaciju izmedju sebe. 

```java
/*
 * Dakle, sistem koji im vise klijenata gde svaki klijent ima listu poslova koje obavlja. 
 * Svaki klijent moze da potrazi pomoc drugih klijenata za neki posao
 * ali samo jedan klijent moze da mu pomogne, a ne svi klijenti koji obavljaju taj posao
 * 
 * 
 * obavestiti sve klijente da je novi klijent registrovan
 * dobijamo odogovore od ostalih klijenata
 * klijent moze da trazi pomoc
 * a klijenti ce mu dati odgovor na tu trazenu pomoc 
*/

public class AtomatizovanaProizvodnja 
{
    public static void main(String[] args)
    {
        Scanner in = new Scanner(System.in);
        int id = GetID(); //funkcija koja generise jedinstven id za klijenta

        ArrayList<String> poslovi = new ArrayList<String> ();

        System.out.println("Klijent " + id + "- unesi broj poslova: ");
        int n = in.nextInt();
        for(int i=1; i <= n; i++) 
        {
            System.out.println("Unesi naziv " + i + ". posla: " + i);
            poslovi.add(in.nextLine());
        }

        Klijent klijent = new Klijent(id, poslovi);
        //kreiramo klijenta sa datim id i prosledimo mu poslove

        System.out.println("Unesite posao za koji Vam je potrebna pomoc: ");
        String pomocZaPosao = in.nextLine();

        klijent.PozoviZaPomoc(pomocZaPosao);

        in.nextLine();
    }
}
```

```java
public class Klijent 
{
    private int id;
    private ArrayList<String> poslovi;

    /*
     * obavestiti sve klijente da je novi klijent registrovan
     * dobijamo odogovore od ostalih klijenata
     * klijent moze da trazi pomoc
     * a klijenti ce mu dati odgovor na tu trazenu pomoc 
    */

    private Topic tNoviKlijent; //publisher i subscriber
    private Queue qNoviKlijentOdgovor; //send i recv

    private Queue qPomoc; //send i recv
    private Queue qPomocOdgovor;//send i recv

    private TopicPublisher tPublisherNoviKlijent;//javi da je kreiran novi klijent
    private TopicSubscriber tSubsriberNoviKlijent;//primi da je kreiran novi klijent

    private QueueSender qSenderNoviKlijentOdgovor; //posalji odgovor na to
    private QueueReceiver qReceiverNoviKlijentOdgovor;//primi odgovor

    private QueueSender qSenderPomoc;//posalji poziv za pomoc
    private QueueReceiver qReceiverPomoc;//primi poziv za pomoc

    private QueueSender qSenderPomocOdgovor;//posalji odgovor na poziv za pomoc
    private QueueReceiver qReceiverPomocOdgovor;//primi odgovor na poziv za pomoc

    private QueueConnection qc; 
    private TopicConnection tc; 
    private QueueSession qs; 
    private TopicSession ts;

    public Klijent(int id, ArrayList<String> poslovi)
    {
        this.id = id;
        this.poslovi = poslovi;

        System.out.println("Klijent " + this.id + " - pribavljanje konteksta.");
        InitialContext ictx = new InitialContext();

        QueueConnectionFactory qcf = (QueueConnectionFactory)ictx.lookup("qcfAutomatizovanaProizvodnja");
        TopicConnectionFactory tcf = (TopicConnectionFactory)ictx.lookup("tcfAutomatizovanaProizvodnja");

        tNoviKlijent = (Topic)ictx.lookup("tNoviKlijent");
        qNoviKlijentOdgovor = (Queue) ictx.lookup("qNoviKlijentOdgovor");
        qPomoc = (Queue) ictx.lookup("qPomoc");
        qPomocOdgovor = (Queue) ictx.lookup("qPomocOdgovor");

        ictx.close();

        qc = (QueueConnection) qcf.createConnection();
        tc = (TopicConnection) tcf.createConnection();

        qs = (QueueSession) qc.createQueueSession();
        ts = (TopicSession) tc.createTopicSession();

        /*SETI SE:
        private TopicPublisher tPublisherNoviKlijent;//javi da je kreiran novi klijent
          private TopicSubscriber tSubsriberNoviKlijent;//primi da je kreiran novi klijent

          private QueueSender qSenderNoviKlijentOdgovor; //posalji odgovor na to
          private QueueReceiver qReceiverNoviKlijentOdgovor;//primi odgovor

          private QueueSender qSenderPomoc;//posalji poziv za pomoc
          private QueueReceiver qReceiverPomoc;//primi poziv za pomoc

          private QueueSender qSenderPomocOdgovor;//posalji odgovor na poziv za pomoc
          private QueueReceiver qReceiverPomocOdgovor;//primi odgovor na poziv za pomoc
        */

        tc.start();

        //publishuj da je kreiran novi klijent
        tPublisherNoviKlijent = (TopicPublisher) ts.createPublisher(tNoviKlijent);
        Message msg = ts.createMessage();
        msg.setIntProperty("id", id);

        tPublisherNoviKlijent.send(msg);
        ts.commit();

        qSenderNoviKlijentOdgovor = (QueueSender)qs.createSender(qNoviKlijentOdgovor);

        //primi da je kreiran novi klijent
        tSubsriberNoviKlijent = ts.createSubscriber(tNoviKlijent);
        tSubsriberNoviKlijent.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message msg) 
            {
                System.out.println("Novi klijent: " + msg.getIntProperty("id"));
                //kad Miki stigne da je Deki novi klijent, on salje odgovor
                //odgovor na novog klijenta salje se sa qSenderNoviKlijentOdgovor
                msg.setIntProperty("id", id);//setujemo na id onog klijenta koji odgovara
                qSenderNoviKlijentOdgovor.send(msg);

                qs.commit();
            }
        });

        //posalji odgovor na kreiranog klijenta - done
        //primi odgovor: 

        qReceiverNoviKlijentOdgovor = (QueueReceiver) qs.createReceiver(qNoviKlijentOdgovor);
        qReceiverNoviKlijentOdgovor.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message msg) 
            {
                //kad primi odgovor, samo ga prima i toliko, ne cini nista sa njim
                System.out.println("Novi klijent (" + msg.getIntProperty("id") + ") - odgovor.");
            }
        });

        //posalji poziv za pomoc - OBRADJUJEMO U POSEBNOJ F-JI
        //primi poziv za pomov - radimo ovde lagano:
        qSenderPomoc = (QueueSender) qs.createSender(qPomoc);//koristimo u posebnoj funkciji
        qSenderPomocOdgovor = (QueueSender) qs.createSender(qPomocOdgovor);//koristicemo ga ispod
        for(String p: poslovi)
        {
            qReceiverPomoc = (QueueReceiver) qs.createReceiver(qPomoc, "pomocZaPosao = '" + p + "'");
            qReceiverPomoc.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message msg)
                {
                    System.out.println("Pomoc za posao - " + p + ", trazi klijent - " + msg.getIntProperty("id"));

                    //kad se primi poziv za pomoc, treba da se da odgovor na taj poziv
                    //taj odgovor se salje sa qSenderPomocOdgovor
                    msg.setIntProperty("id", id);//id onoga ko odgovara
                    msg.setStringProperty("pomocZaPosao", p);
                    qSenderPomocOdgovor.send(msg);
                    qs.commit();
                }
            });
        }

        //posalji odgovor na poziv za pomoc - DONE
        //primi odgovor na poziv za pomoc:

        qReceiverPomocOdgovor = (QueueReceiver) qs.createReceiver(qPomocOdgovor);
        qReceiverPomocOdgovor.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message msg) 
            {
                //kad primi odgovor na poziv za pomoc od nekog klijenta  
                //samo primi taj odgovor, ne radi nista sa tim
                System.out.println("Na poziv za pomoc oko posla " + msg.getStringProperty("pomocZaPosao") + " odgovara klijnet [" + msg.getIntProperty("id") + "]");
            }
        });
    }

    public void PozoviZaPomoc(String posao)
    {
        Message msg = qs.createMessage();
        msg.setStringProperty("pomocZaPosao", posao);
        msg.setIntProperty("id", this.id);

        qSenderPomoc.send(msg);
        qs.commit();
    }

    public void ZatvoriKonekciju()
    {
        qc.close();
        tc.close();
    }
}
```
