# Necentralizovan primer - Cekaonica kod lekara

To je sličaj kada nemamo centralu, već klijenti direktno komuniciraju (hvala bogu...)
Radimo opet na primeru Cekaonice kod lekara. Taj zadatak je složenosti kao zadaci na ispitu. 

![alt text](https://i.imgur.com/wjtM4AK.jpg)

## Lekar.java 

```java
public class Lekar
{
    private Queue qSaljiIzvestaj;
    private Queue qPrimiPacijenta;
    private QueueConnection qc;
    private QueueSession qs;
    private QueueSender qSenderIzvestaj;
    private QueueReceiver qReceiverPacijent;
    public String imeLekara;

    public Lekar(String imeLekara)
    {
        System.out.println("Lekar :: pribavljanje konteksta");
        InitialContext ictx = new InitialContext();
        QueueConnectionFactory qcf = (QueueConnectionFactory)ictx.lookup("qcfCekaonica");
        qSlanjeIzvestaja = (Queue) ictx.lookup("qIzvestaj");//send ka sestri
        qPrimiPacijenta = (Queue) ictx.lookup("qObavestiLekaraOpacijentu");//recv od sestre 

        ictx.close();

        qc = (QueueConnection) qcf.createConnection();
        qs = (QueueSession) qc.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);

        qSenderIzvestaj = (QueueSender) qs.createSender(qSlanjeIzvestaja);

        this.imeLekara = imeLekara;
        //ovaj lekar treba da primi obavestenje o pacijentima koji su njega odabrali za lekara
        //ne treba da dobije obavestenja bas o svakom pacijentu

        qReceiverPacijent = (QueueReceiver) qs.createReceiver(qPrimiPacijenta, "Lekar = '" + imeLekara + "'");
        qc.start();

        //ANONIMNA KLASA:
        qReceiverPacijent.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message msg) 
            {
                TextMessage tmsg = (TextMessage) msg;

                System.out.println("Lekar " + imeLekara + " primio je pacijenta: " + tmsg.getText());

                ObavestiSestru(tmsg.getText());
                //saljemo sestri izvestaj - ime pacijenta koji je pregledan
            }
        });

        System.out.println("Lekar :: izvrsena inicijalizacija.");
    }

    public void ObavestiSestru(String pacijent) 
    {
        TextMessage tmsg = qs.createTextMessage(pacijent);

        qSenderIzvestaj.send(tmsg);

        qs.commit();
    }

    public static void main(String[] args) 
    {
        Scanner in = new Scanner(System.in);
        System.out.println("Unesite ime lekara: ");
        String imeLekara = in.nextLine();

        Lekar lekar = new Lekar(imeLekara);

        in.next();
    }
}
```

## MedicinskaSestra.java

```java
public class MedicinskaSestra
{
    private QueueConnection qc;
    private QueueSession qs;
    private Queue qZaLekara;
    private Queue qZaStampu;
    private QueueSender qSenderPacijenta;
    private QueueReceiver qReceiverIzvestaja;

    public MedicinskaSestra()
    {
        System.out.println("Medicinska sestra :: pribavljanje konteksta");

        InitialContext ictx = new InitialContext();

        QueueConenctionFactory qcf = (QueueConenctionFactory)ictx.lookup("qcfCekaonica");
        qZaLekara = (Queue) ictx.lookup("qObavestiLekaraOpacijentu");
        qZaStampu = (Queue) ictx.lookup("qIzvestaj");

        ictx.close();

        qc = (QueueConnection) qcf.createConnection();
        qs = (QueueSession) qc.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);

        qSenderPacijenta = (QueueSender) qs.createSender(qZaLekara);

        qReceiverIzvestaja = (QueueReceiver) qc.createReceiver(qZaStampu);
        qc.start();

        qReceiverIzvestaja.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) 
            {
                TextMessage tmsg = (TextMessage) message;

                System.out.println("Pregledan je pacijent " + tmsg.getText());
            }
        });
    }

    public void ObavestiLekara(String pacijent, String lekar)
    {
        TextMessage tmsg = qs.createTextMessage(pacijent);

        tmsg.setStringProperty("Lekar", lekar);
        qSenderPacijenta.send(tmsg);

        qs.commit();
    }

    public static void main(String[] args)
    {
        MedicinskaSestra medSestra = new MedicinskaSestra();

        Scanner in = new Scanner(System.in);

        System.out.println("Unesite ima pacijenta: ");
        String pacijent = in.nextLine();

        System.out.println("Unesite odabranog lekara: ");
        String lekar = in.nextLine();

        medSestra.ObavestiLekara(pacijent, lekar);

        System.in.read();
    }
}
```
