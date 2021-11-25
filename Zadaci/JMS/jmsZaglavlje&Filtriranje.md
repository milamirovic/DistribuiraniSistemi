# JMS zaglavlje i filtriranje poruke

# JMS zaglavlje

JMS zaglavlje sastoji se iz 2 vrste propertija podeljene po odgovornosti: 
- one koje podešavaju developeri
- one koje podešava JMS automatski

Obe grupe propertija dostupne su preko *get* i *set* metoda. 

Podrazumevano zaglavlje sadrži:
- **JMSDestination** - odredište poruke (kome je namenjena  poruka)
- **JMSDeliveryMode** - definiše način dostave (persistant ili non-persistant poruka/ ostaje u sistemu nakon isporuke ili se automatski briše nakon isporuke)
- **JMSPriority** - podešava producer, 0-4 normalan and 5-9 neodložan (da li će poruka odmah da bude obrađena ili može neko vreme da provede u sistemu)

Podesivo (custom) zaglavlje:
- JMSReplyTo - definiše odredište odgovora (kome treba da odgovori nakon što obradi poruku)
- JMSType - definoše tip poruke, ne podešava se direktno

Sami propertiji su podeljeni u 3 tipa:
- **Aplikacijsko specifični** - odnose se na podatke koji se mogu pridodati poruci i mi ih kao developeri definišemo. (koristićemo ih)
- JMS definisani - automatski setovani od strane JMS providera (nisu nam od značaja)
- Provider specifični - automatski se setuju od strane JMS providera (nisu nam od značaja)

Njihova vrednost može biti *String*, *boolean*, *byte*, *double*, *long* ili *float*. 

# JMS telo tj. teret (PAYLOAD) poruke

U zavisnosti od sadržaja koji prenosimo porukom, poruka može biti:
- Message
- TextMessage
- StreamMessage
- MapMessage
- ObjectMessage
- BytesMessage

Ovo su 6 tipova poruka koje JMS API podržava. Pored njih mogu se generisati i novi tipovi poruka. Svi ovi tipovi poruka izvedeni su iz interfejsa Message. Može se slati i čist Message tip, ako je potrebno da se pošalje događaj. 

# JMS filtriranje poruke (Message Selectors)

Pošto DS ima čvorove na različitim geografskim lokacijama, ne obavlja se sve na 1 računaru, potrebno je dosta poruka poslati između svakog od čvorova, da bi poruka stiga na ispravno mesto, što želimo da izbegnemo. Da se to izbegne i da poruku dobije samo onaj kome je ona namenjena, koristimo FILTRIRANJE. 

Filtriranje smanjuje broj poslatih poruka i kreiramo Network Balancing - samo one poruke koje moraju da odu zaista odu. Obavlja se na Producer strani tj. na JMS provideru. On je zadužen da tu poruku distribuira svim subscriberima za taj destination. Filtriranje se obavlja na **SQL92** standardu tj. pišemo jedan deo **where deo query-a** nad jednom tabelom:
> ```java
> ...
> topic = (Topic)ctx.lookup(topicName);
> ...
> String filter = "your condition";
> TopicSubscriber subscriber = session.createSubscriber(topic, filter, true);
> ...
> ```

Podržani su operatori LIKE, BETWEEN, IN, NOT i IS NULL.

Kod Point-to-point, jednom filtrirana poruka se uklanja iz queue-a i nije više dostupna ostalima. Mogu se koristiti prioriteti, prvo se filtriraju poruke sa višim prioritetom. 

Filter možemo da primenimo na consumer-e, pa filtere koristimo u QueueReceiver-u (ako je u pitanju queue) ili u TopicSubscriber-u (ako koristimo topic).

**U filteru se može koristiti svaki property koji se nalazi u zaglavlju poruke, ali ne može da se koristi payload - sadržaj poruke. Dakle, bitne stvari smešatmo u propertije.**

Primer. Poruku šaljemo samo ako je ime "distribuirani" i ako je prioritet veći od 5, pa to izlgeda ovako:
```java
Name='distribuirani' AND JMSPriority>5
```

## Zadatak. Generisati centralni IS koji omogućava sestri da prijavi pacijenta koji je došao kod određenog lekara. Po okončanju posete treba da odštampa informacije o pregledu. Iz uloge lekara dobija se koji je pacijent na redu, po završetku lekar obaveštava da je završio pregled. Uvedena i treća komponenta - ROTO čekaonica - prikazuje listu trenutnih pacijenata po lekaru. 

![alt text](https://i.imgur.com/Bz1whpv.jpg)

Pošto je ovo CENTRALIZOVANI SISTEM smatramo da imamo jednog klijenta - CENTRALA - koji obavlja komunikaciju između svih ostalih čvorova. Dakle, sestra ne može direktno da priča sa lekarom, nego sva komunikacija ide kroz centralu. Kada dođe pacijent sestra bira lekara i prosledi info centrali. Centrala to treba da prosledi odgovarajućem lekaru i odgovarajućoj ROTO čekaonici (da bi nam ona pokazala sve pacijente tog lekara koji čekaju na prijem). 

Kada lekar završi pregled, on vraća centrali da je traženi pacijent završen, centrala obaveštava sestru da može da štampa podatke o pregledu i obaveštava ROTO čekaonicu da bi ažurirala svoj prikaz. 

1 komunikacioni kanal služi za samo 1 tip poruka. Ako imamo više različitih poruka koje šaljemo, imaćemo više različitih komunikacionih kanala. Ako odemo u radnju da kupimo hleb i u istom smo redu sa nekim ko hoće da plati struju, sa nekim ko kupuje auto, sa nekim ko traži procenu svoje nekretnine itd. To su sve potpuno različita tipa posla. Ako sve vučemo kroz isti KK poruke neće da se procesiraju na vreme. 

### Sestra.java
```java
//asistent ne ocekuje import, try, catch, throws delove na ispitu!
 
public class Sestra
{
 
    private final QueueConnection qc;
    private final QueueSession qs;
    private final QueueSender sender;
     
    public Sestra()
    {
        System.out.println("Sestra :: pribavljanje contexta");
 
        //pribavimo kontext
        InitialContext ictx = new InitialContext();
        //pribavimo connectionFactory
        QueueConnectionFactrory qcf = (QueueConnectionFactrory) ictx.lookup("qCentrala");
        //pribavimo destination:
        //pribavimo queue za obavestavanje lekara:
        Queue obavestiLekara = (Queue)ictx.lookup("qObavestiCentralu");//odlazni queue
        //1 komunikacioni kanal sluzi za samo 1 tip poruka
        //ako imamo vise razlicitih poruka koje saljemo, imacemo vise razlicitih komunikacionih kanala
        //zato imamo poseban queue za stampanje podataka o pregledu pacijenta:
        Queue stampa = (Queue) ictx.lookup("stampa");
 
        ictx.close();
 
        //pribavljamo konekciju
        qc = (QueueConnection) qcf.createConnection();
        qs = (QueueSession) qc.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
        //true - prilikom slanja poruke mora qs.commit(), pa se sve poruke posalju tada!!!
        //false - poruke se automatski salju kada ih stavimo na send()
 
        sender = (QueueSender) qs.createSender(obavestiLekara);
         
        QueueReceiver receiver = (QueueReceiver) qs.createReceiver(stampa);
        //sto se tice samog podesavanja poruke, treba nam MessageListener:
        receiver.setMessageListener(new StampaML());
        //klasu StampaML moramo takodje da napisemo, implementira MEssageListener
 
        //sada pokrecemo slusanje:
        qc.start();
        System.out.println("Sestra :: inicijalizacija gotova!");
    }
 
    public void ObavestiLekaraDaJePacijentStigao(String lekar, String pacijent) 
    {
        TextMessage msg = qs.createTextMessage(pacijent);//dodelimo text u kontruktoru
        //ako je bilo Message msg onda ne moze da se uradi msg.setText(...)!!!
        //msg.setText(pacijent);//mozemo da dodelimo text i preko metode setText
 
        //da bi se znalo kom lekaru se salje ovo obavestenje, da ne ode svima
        msg.setStringProperty("Lekar", lekar);
 
        sender.send(msg);

        qs.commit();//jer je gore true!!!
    }
 
    public void ZatvoriKonekciju() 
    {
        qc.close();
    }
 
    public static void main(String[] args)
    {
        Sestra s = new Sestra();
 
        Scanner in = new Scanner(System.in);
 
        System.out.println("Unesite pacijenta:");
        String p = in.nextLine();

        System.out.println("Unesite lekara:");
        String l = in.nextLine();
        s.ObavestiLekaraDaJePacijentStigao(l, p);

        System.in.read();

        s.ZatvoriKonekciju();
    }
}
```

### StampaMS.java
```java
public class StampaML implements MessageListener 
{
    public StampaML() 
    {

    }

    public void onMessage(Message msg) 
    {
        TextMessage txt = (TextMessage) msg;

        System.out.println("Pregledan pacijent :: " + txt.getText());
    }
}
```

### Lekar.java
```java
//asistent ne ocekuje import, try, catch, throws delove na ispitu!

public class Lekar
{

    private final QueueConnection qc;
    private final QueueSession qs;
    private final QueueSender sender;
    private final TopicConnection tc;
    private final TopicSession ts;
    private final Topic obavestiLekara;
    public String NazivLekara;
    
    public Lekar()
    {
        System.out.println("Lekar :: pribavljanje contexta");

        //pribavimo kontext
        InitialContext ictx = new InitialContext();
        //pribavimo connectionFactory
        QueueConnectionFactrory qcf = (QueueConnectionFactrory) ictx.lookup("qCentrala");
        TopicConnectionFactory tcf = (TopicConnectionFactory) ictx.lookup("tCentrala");
        //pribavimo destination:
        //pribavimo queue za obavestavanje lekara:
        obavestiLekara = (Topic)ictx.lookup("tObavestiLekara");//dolazni topic
        Queue stampa = (Queue) ictx.lookup("qStampaCentrala");//odlazni queue

        ictx.close();

        //pribavljamo konekcije:
        qc = (QueueConnection) qcf.createConnection();
        tc = (TopicConnection) tcf.createConnection();
        //pribavljamo sesije:
        qs = (QueueSession) qc.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
        ts = (TopicSession) tcf.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);

        //mi od lekara saljemo informacije za med.sestru da stampa podatke o pregledu
        //zato je sender kreiran sa - stampa:
        sender = (QueueSender) qs.createSender(stampa);
    }

    public void ObavestiCentralu(String l)
    {
        TextMessage msg = qs.createTextMessage();
        msg.setStringProperty("Lekar", l);

        sender.send(msg);

        qs.commit();
    }

    private void ObavestiCentralu(String l, String p) 
    {
        TextMessage msg = qs.createTextMessage(p);
        msg.setStringProperty("Lekar", l);

        sender.send(msg);

        qs.commit();
    }

    public void KreniSaRadom(String l) 
    {
        NazivLekara = l;
        ObavestiCentralu(NazivLekara);
        //setujemo filtere:
        //U SQL-u kada se poredi STRING mora da stoji pod apostrofima 'str'
        receiver = (TopicSubscriber) ts.createSubscriber(obavestiLekara, "Lekar = '" + l +"'", true);
        receiver.setMessageListener(new LekarMS(this));//moramo da definisemo LekarMS klasu!

        qc.start();
        tc.start();
    }

    public void ZatvoriKonekciju() 
    {
        qc.close();
        tc.close();
    }

    public static void main(String[] args)
    {
        Lekar lekar = new Lekar();

        Scanner in = new Scanner(System.in);

        System.out.println("Unesite lekara:");
        String l = in.nextLine();

        //podesimo filtere 
        lekar.KreniSaRadom(l);

        System.out.println("Lekar :: inicijalizacija gotova");

        System.in.read();

        s.ZatvoriKonekciju();
    }
}
```

### LekarMS.java
```java
public class LekarML implements MessageListener 
{
    Lekar lekar;
    public LekarML(Lekar l) 
    {
        lekar = l;
    }

    public void onMessage(Message msg) 
    {
        TextMessage txt = (TextMessage) msg;

        System.out.println(lekar.NazivLekara + " :: Primio pacijenta :: "+txt.getText());
        lekar.ObavestiCentralu(lekar.NazivLekara, txt.getText());
    }
}
```

### Centrala.java
```java
public class Centrala
{
    private Topic obavestiLekara;
    private QueueConnection qc;
    private QueueSession qs;
    private TopicConnection tc;
    private TopicSession ts;
    private QueueSender senderStampa;
    private TopicPublisher obavestiLekar;
    private Topic tRotoPromena;
    private TopicPublisher roto;

    public Centrala()
    {
        System.out.println("Pribavljanje contexta");
        InitialContext ictx = new InitialContext();

        QueueConncetionFactory qcf = (QueueConnectionFactory) ictx.lookup("qCentrala");
        TopicConnectionFactory tcf = (TopicConnectionFactory) ictx.lookup("tCentrala");

        obavestiLekara = (Topic) ictx.lookup("tObavestiLekara");
        Queue stampaCentrala = (Queue) ictx.lookup("qStampaCentrala");
        Queue stampa = (Queue) ictx.lookup("qObavestiCentralu");

        ictx.close();

        qc = (QueueConnection) qcf.createConnection();
        qs = (QueueSession) qc.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);

        tc = (TopicConnection) tcf.createConnection();
        ts = (TopicSession) tcf.createTopicSession(true, Session.AUTO_ACKNOWLEDGE);

        System.out.println(">>>Centrala - Receive & Send<<<");

        senderStampa = (QueueSender)qs.createSender(stampa);

        QueueReceiver receiverLekar = (QueueReceiver) qs.createReceiver(obavestiLekara);
        receiverLekar.setMessageListener(new RedirectLecarML(this));

        QueueReceiver receiverStampa = (QueueReceiver) qs.createReceiver(stampaCentrala);
        receiverStampa.setMessageListener(new RedirectStampaML(this));

        obavestiLekar = (TopicPublisher) ts.createPublisher(obavesitLekara);

        roto = (TopicPublisher)ts.createPublisher(tRotoPromena);
    }

    public void RedirectLekar(Message msg)
    {
        //poruka u sebi sadrzi pacijenta kao text i sadrzi property lekar koji sluzi za filter
        obavestiLekar.send(msg);
        ts.commit();
    }

    public void RedirectStampa(Message msg)
    {
        //saljemo sestri
        senderStampa.send(msg);
        //ovo mora da se prosledi i svakoj ROTO cekaonici
        //da ne bismo vodili racuna do kakve promene je doslo, msg cemo malo promeniti
        TextMessage txt = (TextMessage) msg;

        TextMessage tmsg = new TextMessage("Pregledan " + txt.getText() + " kod lekara " + txt.getStringProperty("Lekar"));
        tmsg.setStringProperty("Lekar", txt.getStringProperty("Lekar"));

        roto.send(msg);
        ts.commit();

    }

    public void Zatvori()
    {
        qc.close();
        tc.close();
    }

    public static void main(String[] args)
    {
        Centrala c = new Centrala();

        System.in.read();
        c.Zatvori();
    }
}
```

### RedirectStampaML.java
```java
public class RedirectStampaML implements MessageListener
{
    Centrala c;

    public RedirectStampaML(Centrala aThis)
    {
        c = aThis;
    }

    public void onMessage(Message msg)
    {
        c.RedirectStampaML(msg);
    }
}
```

### RedirectLekarML.java
```java
public class RedirectLekarML implements MessageListener
{
    Centrala c;

    public RedirectStampaML(Centrala aThis)
    {
        c = aThis;
    }

    public void onMessage(Message msg)
    {
        c.RedirectLekarML(msg);
    }
}
```
