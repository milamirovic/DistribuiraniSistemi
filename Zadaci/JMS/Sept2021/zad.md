# Septembar 2021 - LOTO

```java
/*
 * LOTO - Sep2021
 * Klijent - id, kombinacija (moze imati vise kombinacija);
 * EksterniSistem (ne implementiraj) - izvlaci brojeve
 * 
 */

public class Loto 
{
    private ArrayList<Klijent> klijenti;
    private TopicConnection tc;
    private TopicSession ts;

    private Topic tIzvucenBroj;
    private Queue qSvePotencijalneDobitneKombinacije;
    private Queue qNemaBrojaUKombinacijama;

    private TopicPublisher tPublisherIzvucenBroj;
    private QueueReceiver qReceiverSvePotencijalneDobitneKombinacije;
    private QueueReceiver qReceiverNemaBrojaUKombinacijama;

    public void addKlijent()
    {
        Scanner in = new Scanner(System.in);
        int id = GetId();//funkcija koja vraca jedinstven id
        ArrayList<ArrayList<Integer>> kombinacije = new ArrayList<ArrayList<Integer>>();

        System.out.println("Da li zelite da uneste novu kombinaciju? (DA ili NE)");
        String odg = in.nextLine();
        int br = 0;//br kombinacija
        while(odg=="DA")
        {
            br++;
            System.out.println("Unesite svoju kombinaciju od 7 brojeva. Moguci brojevi su od 1 do 39!");
            ArrayList<Integer> kombinacija = new ArrayList<Integer>();
            for(int i=1;i<=7;i++)
            {
                System.out.println("Unesi " + i + ". broj: ");
                kombinacija.add(in.nextInt());
            }
            kombinacije.add(kombinacija);
            System.out.println("Kombinacija je usmesno uneta! Da li zelite da uneste novu kombinaciju? (DA ili NE)");
        }
        this.klijenti.addKlijent(new Klijent(id, kombinacije));
    }

    public Loto()
    {
        klijenti = new ArrayList<Klijent>();

        InitialContext ictx = new InitialContext();
        TopicConnectionFactory tcf = (TopicConnectionFactory)ictx.lookup("tcfLoto");
        tIzvucenBroj = (Topic) ictx.lookup("tIzvucenBroj");
        qSvePotencijalneDobitneKombinacije = (Queue) ictx.lookup("qSvePotencijalneDobitneKombinacije");
        qNemaBrojaUKombinacijama = (Queu) ictx.lookup("qNemaBrojaUKombinacijama");

        ictx.close();

        tc = (TopicConnection) tcf.createConnection();
        ts = (TopicSession)tc.createTopicSession(true, Session.AUTO_ACKNOWLEDGE);

        tPublisherIzvucenBroj = (TopicPublisher)ts.createPublisher(tIzvucenBroj);

        tc.start();

        qReceiverSvePotencijalneDobitneKombinacije = (QueueReceiver)qs.createReceiver(qReceiverSvePotencijalneKombinacije);
        qReceiverNemaBrojaUKombinacijama = (QueueReceiver)qs.createReceiver(qReceiverNemaBrojaUKombinacijama);

        qc.start();

        qReceiverSvePotencijalneDobitneKombinacije.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message msg) 
            {
                TextMessage tmsg = (TextMessage)msg;
                //u tekstu je spisak svih moguvih dobitnih kombinacija
                //u propertiju je id klijenta 
                System.out.println("Klijent [" + tmsg.getIntProperty("id") + "] ima sledece potencijalne dobitne kombinacije:");
                System.out.println(tmsg.getText());
                //loto je to primo k znanju, i ne radi nista sa tom informacijom
            }
        });

        qReceiverNemaBrojaUKombinacijama.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message msg) 
            {
                TextMessage tmsg = (TextMessage)msg;
                System.out.println("Klijent [" + tmsg.getIntProperty("id") + "]:");
                System.out.println(tmsg.getText());
            }
        });

    }

    public void PublishujIzvuceniBroj(Integer broj)
    {
        TextMessage msg = ts.createTextMessage(broj);
        tPublisherIzvucenBroj.send(msg);
        ts.commit();
    }

    public static void main(String[] args)
    {
        Loto loto = new Loto();
        Scanner in = new Scanner(System.in);

        loto.addKlijent();
  
        EksterniSistem lotoMasina = new EksterniSistem();
        ArrayList<Integer> izvuceniBrojevi = new ArrayList<Integer>();

        for(int i=0;i<7;i++)
        {
            int izvucenBroj = lotoMasina.izvuciBroj();
            //posalji izvucen broj svim klijentima - sadrzaj poruke je izvuceni broj
            loto.PublishujIzvuceniBroj(izvucenBroj);
        }
    }
}
 

public class Klijent
{
    private int id;
    private ArrayList<ArrayList<Integer>> kombinacije;

    private TopicPublisher tPublisherKombinacije;//posalji svima svoju kombinaciju i id
    private TopicSubscriber tSubscriberKombinacije;//primi kombinacije drugih klijenata

    private QueueReceiver qReceiverPrimiIzvucenBroj;//prima izvucen broj od Loto-a

    private QueueSender qSenderPrikaziSvePotencijalneDobitneKombinacije;//send ka Loto  
    private QueueSender qSenderNemaBrojaUKombinacijama;

    private QueueConnection qc;
    private TopicConnection tc;
    private QueueSession qs;
    private TopicSession ts;

    private Topic tKombinacija;
    private Queue qPrimiIzvucenBroj;
    private Queue qSvePotencijaleDobitneKombinacije;
    private Queue qNemaBrojaUKombinacijama;

    public Klijent(int id, ArrayList<ArrayList<Integer>> kombinacije)
    {
        this.id = id;
        this.kombinacije = kombinacije;

        InitialContext ictx = new InitialContext();
        QueueConnectionFactory qcf = (QueueConnectionFactory)ictx.lookup("qcfLoto");
        TopicConnectionFactory tcf = (TopicConnectionFactory)ictx.lookup("tcfLoto");

        tKombinacije = (Topic) ictx.lookup("tKombinacije");
        qPrimiIzvucenBroj = (Queue) ictx.lookup("qIzvucenBroj");
        qSvePotencijaleDobitneKombinacije = (Queue) ictx.lookup("qSvePotencijalneDobitneKombinacije");
        qNemaBrojaUKombinacijama = (Queue) ictx.lookup("qNemaBrojaUKombinacijama");

        ictx.close();

        qc = (QueueConnection)qcf.createConnection();
        tc = (TopicConnection)tcf.createConnection();

        qs = qc.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
        ts = tc.createTopicSession(true, Session.AUTO_ACKNOWLEDGE);

        tPublisherKombinacije = ts.createPublisher(tKombinacije);
        TextMessage msg = ts.createTextMessage();
        //salje svoju kombinaciju, a moze da ih ima vise, pa salje svaku od njih:
        for(ArrayList<Integer> kombinacija: kombinacije)
        {
            String komb = "";
            for(Integer i: kombinacija)
            {
                komb += i.toString();
                komb += " ";
            }
            msg.setText(komb);
            msg.setIntProperty("id", this.id);
            tPublisherKombinacije.send(msg);
        }
        
        //primi kombinacije drugih klijenata
        tSubscriberKombinacije = ts.createSubscriber(tKombinacije);
        tSubscriberKombinacije.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message msg) 
            {
                TextMessage tmsg = (TextMessage)msg;
                System.out.println("Kombinacija klijenta [" + tmsg.getIntProperty("id") + "] je: " + tmsg.getText());
                //kad klijent primi ovakvo obavestenje, samo to prima k znanju, ne radi nista sa tim
                //tako da, to je to
            }
        });

        //primi izvucen broj od Loto objekta
        qReceiverPrimiIzvucenBroj = (QueueReceiver) qs.createReceiver(qIzvucenBroj);
        qReceiverPrimiIzvucenBroj.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message msg) 
            {
                TextMessage tmsg = (TextMessage)msg;
                ObradiPrimljenIzvucenBroj(tmsg);
            }
        });
    }

    public void ObradiPrimljenIzvucenBroj(TextMessage tmsg)
    {
        int izvucenBroj = Integer.parseInt(tmsg.getText());

        ArrayList<String> potencijalneDobitneKombinacije = new ArrayList<String>();
        TextMessage tmsg = ts.createTextMessage();

        for(ArrayList<Integer> kombinacija: this.kombinacije)
        {
            if(kombinacija.contains(izvucenBroj))
            {
                String kombStr = "";
                for(Integer i: kombinacija)
                {
                    kombStr += i.toString();
                    kombStr += " ";
                }
                potencijalneDobitneKombinacije.add(kombStr);
            }
        }

        if(potencijalneDobitneKombinacije.size()>0)
        {
            //ima potencijalnih dobitnih kombinacija
            qSenderPrikaziSvePotencijalneDobitneKombinacije = qs.createSender(qSvePotencijaleDobitneKombinacije);
            String text = "";
            for(String pdk: potencijalneDobitneKombinacije)
            {
                text += pdk + "|";
            }
            tmsg.setText(text);
            tmsg.setIntProperty("id", this.id);

            qSenderPrikaziSvePotencijalneDobitneKombinacije.send(tmsg);

            qs.commit();
        }
        else if(potencijalneDobitneKombinacije.size() == 0)
        {
            qSenderNemaBrojaUKombinacijama = qs.createSender(qNemaBrojaUKombinacijama);
            tmsg.setText("U kombinacijama nema datog broja!");
            tmsg.setIntProperty("id", this.id);

            qSenderNemaBrojaUKombinacijama.send(tmsg);

            qs.commit();
        }
    }
}

```
