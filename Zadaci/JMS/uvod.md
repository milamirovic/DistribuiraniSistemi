# JMS - Java Message Service

* Message Oriented Computing - obrada zasnovana na porukama. 
* JMS API

## MOM - Message Oriented Medium
Medjusloj za slanje i primanje poruka izmedju zainteresovanih strana. Postoji problem u Informacionim sistemima, a to je ASINHTRONA KOMUNIKACIJA. MOM omogućava laku integraciju heterogenih sistema, dobro rešava problem uskog grla, povećava ukupnu propusnu moć sistema i poboljšava fleksibilnost sistema. 

MOM je Message provider - JMS provider - koji komunicira sa Messaging client-om. Messaging client je deo koji je zadužen za komunikaciju između naše aplikacije (klijentske aplikacije) i samog MOM-a. Sva komunikacija između dva klijenta ide kroz MOM, ne može se poruka poslati direktno, nego kroz MOM. 

![alt text](https://i.imgur.com/FHo8EEa.jpg)

## MOM tipovi komunikacije:
1. **Sinhroni**
> pošiljalac blokiran sve dok primalac ne dobije poruku (ako ja šaljem poruku ne smem da radim ništa sve dok ta poruka ne bude isporučena ili obrađena)
2. **Asinhroni**
> Pošiljalac nastavlja sa radom nakon slanja poruke (zaboravlja na poruku, kad god da je primljena ili obrađena, nije bitno)
3. **Tranzijentni**
> Tranzijentni su privremeni - pošiljalac i primalac aktivni dok se poruka dostavlja. Oba učesnika obavezno moraju biti prisutna, kao u telefonskom pozivu. 
4. **Perzistentni**
> Perzistentni su trajni - poruka se čuva u komunikacionom sistemu sve dok njeno dostavljanje ne bude bilo moguće. Nešto kao sms poruka. 

Da bismo koristili MOM potrebno je da Informacioni sistem bude kreiran u obliku serverski orijentisane arhitekture. To se postiže na 2 moguća načina:
1. **Enterprise Servise Bus (ESB) pristup**
> * poruke se razmenjuju asinhrono
> * koristi se API za kreiranje poruka i slanje kroz MOM
> * poruke predstavljaju autonomne celine, sadrže sve podatke i stanja neophodna za rad biznis logike
2. **Event driven - pristup zasnovan na događajima**
> * komunikacija se obavlja po asinhronoj šemi
> * poruke se šalju na efikasan i robustan način
> * poruke su *self-described* - sadrže sve neophodne informacije koje omogućavaju prijemniku 
> * loosly coupled - sve komponente su slabo spregnute

Kao što se može primetiti, ova dva načina su praktično identična. 

## Arhitektura Service Oriented Architecture (SOA)

Sama SOA može biti **centralizovana** i **decentralizovana**. 

![alt text](https://i.imgur.com/0XK4sT9.png)

* Centralna arhitektura ima jedini pravi servis i to je Java Message Service - JMS. 
* Decentralizovana arhitektura nema Message Server, već sam mrežni sloj igra ulogu distribuiranja poruka. 

## Modeli komunikacije 

* **Sinhrona komunikacija**
> * oba učesnika komunikacije moraju biti aktivna
> * pošiljalac dobija od prijemnika potvrdu o prijemu 
> * blokirajući pozivi
> * podržava scenario gde je autorizacija obavezna (upotreba kreditnih kartica npr.)

* **Asinhrona komunikacija** 
> * u toku komunikacije ne moraju oba učesnika da budu aktivna (prijemna strana ne mora da bude aktivna)
> * konfirmacija nije neophodna od strane prijemnika
> * nema blokirajućih poziva
> * korisno kada se zahteva procesiranje masovne komunikacije (kada šaljemo poruku većem broju primalaca)
> * pošto nije blokirajući, omogućava efikasnu upotrebu hardverskih resursa, jer se nastavlja sa radom, nema čekanja na odgovor od prijemnika

* **Point-to-point model komunikacije**
  
![alt text](https://i.imgur.com/VLQ6W3n.jpg)

> * poruku dobija isključivo jedan primaoc, i šalje je samo jedan pošiljaoc - komunikacija 1 - 1
> * onaj koji proizvodi podatke je **sender**, primaoc je **receiver**. 
> * Pošiljaoci i primaoci komuniciraju preko virtuelnih kanala koji se zovu redovi (**queues**).
> * Point-to-point model podržava i asinhronu i sinhronu komunikaciju
> * Pošiljaoc može zahtevati novu poruku u bilo kom trenutku
> * Servisi su jače spregnuti, pošiljaoc obično zna primaoic i koje informacije on očekuje
> * queue je sastavni deo Message Servisa, sender šalje Message Servisu poruku na odgovarajući queue, a queue odlučuje kom od recievera će proslediti poruku (samo 1 receiver-u)

* **Publish-Subscribe model komunikacije**

![alt text](https://i.imgur.com/wftpHjp.jpg)

> * Oni koji objavljuju podatke su **publishers/producer**, a oni koji ih primaju i koriste su **subscribers/consumer**. 
> * Poruke se objavljuju preko virtuelnih kanala koji se zovu teme (**topics**).
> * Poruke se broadcast-uju svim subscriber-ima, svaki subscirber dobija svoju ličnu kopiju poruke
> * Komunikacija je 1 - više
> * Servisi su slabije spregnuti u odnosu na Point-to-point model

# Java Message Service (JMS) 
Apstraktni API za kreiranje poruka, nije kompletan sistem. API se može podeliti na 3 celine:

1. General API (za komunikaciju i queues i topics)
2. Point-to-point API
3. Publish-Subscribe API
 
## JMS General API - BITNO
Sve što budemo radili zasniva se na ovoj slici:

![alt text](https://i.imgur.com/gje3qUQ.jpg)

Glavni interfejsi su:
- **ConnectionFactory**
- **Destination**
- **Connection**
- **Session**
- **Message**
- **MessageProducer**
- **MessageConsumer**

Podaci teku kroz ove interfejse. Da bismo uradili bilo šta moramo da:
1. Preuzmemo **ConnectionFactory** i **Destination** - queue i/ili topic.
2. Na osnovu njih generišemo **konekciju** i **sesiju**. 
3. U zavisnosti od toga da li smo producer ili consumer, generisaćemo MessageProducer i Message ili MessageConsummer i Message. 

### Primer.

#### Producer.java
> ```java
> public class Producer 
> {
>       static Context ictx = null;
>       public static void main(String[] args) throws Exception
>       {
>           ictx = new InitialContext();
>           //moramo preuzeti ConnectionFactory iz InitialContext-a
>           ConnetionFactory cf = (ConnectionFactory) ictx.lookup("jms/_defaultConnectionFactory");
>           //moramo preuzeti queue i topic iz InitialContext-a (to su Destination-i)
>           Queue queue = (Queue) ictx.lookup("queue");
>           Topic topic = (Topic) ictx.lookup("topic");    
>           ictx.close();
>           //kreiramo konekciju:
>           try(Connection cnx = cf.createConnection())
>           {
>               //kreiramo sesiju za konekciju:
>               Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
>               //generisemo producer iz sesije
>               MessageProducer prod = sess.createProducer(null);
>               //generisemo text message iz sesije
>               TextMessage msg = sess.createTextMessage();
>               for(int i=0;i<10;i++) 
>               {
>                   msg.setText("Test number " + i);
>                   //vrsimo slanje:
>                   prod.setText(queue, msg);
>                   prod.send(topic, msg);
>               }
>           }
>       }
> }
> ```

#### Subscriber.java
> ```java
> public class Subscriber 
> {
>     static Context ictx = null;
> 
>     public static void main(String[] args) throws Exception
>     {
>         ictx = new InitialContext();
>         ConenctionFactory cf = (ConnectionFactory) ictx.lookup("jms/_defaultConnectionFactory");
>         Queue queue = (Queue) ictx.lookup("queue");
>         Topic topic = (Topic) icts.lookup("topic");
>         ictx.close();
> 
>         Connection cnx = cf.createConnection();//kreiramo konekciju
>         Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);//kreiramo sesiju za konekciju
>         MessageConsumer recv = sess.createConsumer(queue);//iz sesije kreiramo receivere
>         MessageConsumer subs = sess.createConsumer(topic);
>         //vrsi se ASINHRONO slusanje sa MsgListener
>         recv.setMessageListener(new MsgListener("Queue"));//setujemo prijem
>         subs.setMessageListener(new MsgListener("Topic"));
>         //MsgListener radi ASINHRONO
>         //MsgListener implementira MessageListener interfejs koji je sastavni deo API-a
>         //on ima jednu jedinu metodu onMessage koja se override-uje
>         cnx.start();
>         System.in.read();
>         cnx.close();
>     }
> }
> ``` 

## JMS Point-to-point API  
Na prvi pofgled izgleda kao global API, a zapravo, razlika je u tome što ispred svakog interfejsa stoji "Queue" i Destination je samo queue (nema topic). 

![alt text](https://i.imgur.com/rR7hw0K.jpg)

Producer je **QueueSender**, Consumer je **QueueReceiver**.

Radnje koje bi trebalo obaviti:

Sender:
- preuzmi referencu za ConnectionFactory iz InitialContext-a
- preuzmi referencu za Destination - Queue
- kreiraj QueueConnection konekciju
- kreiraj QueueSession sesiju
- kreiraj QueueSender
- kreiraj Message 
- pošalji poruku Message 

Receiver:
- preuzmi referencu za ConnectionFactory iz InitialContext-a
- preuzmi referencu za Destination - Queue
- kreiraj QueueConnection konekciju
- kreiraj QueueSession sesiju
- kreiraj QueueReceiver receiver-a
- čekaj na poruku (sinhrono) ili implementiraj MessageListener (asinhrono)

### Primer.

#### Sender.java 
> ```java
> public class Sender 
> {
>     static Context ictx = null;
> 
>     public static void main(String[] args) throws Exception
>     {
>         ictx = new InitialContext();
>         //generisemo queue
>         Queue queue = (Queue) ictx.lookup("queue");
>         QueueConnetionFactory qcf = (QueueConnetionFactory) ictx.lookup("jms/__defaultConnectionFactory");
>         //kada zavrsimo sa radom sa intial contextom, zatvorimo ga!
>         ictx.close();
> 
>         QueueConnection qc = qcf.createQueueConnection();//konekcija
>         QueueSession qs = qc.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
>         QueueSender qsend = qs.createSender(queue);//generisemo sednera
>         TextMessage msg = qs.createTextMessage();//kreiramo poruku
>         for(i=0;i<10;i++) 
>         {
>             msg.setText("Test number " + i);//setujemo tekst za poruku
>             qsend.send(msg);//saljemo poruku
>         }
>     }
> }
> ```

#### Receiver.java 
> ```java
> public class Receiver 
> {
>     static Context ictx = null;
> 
>     public static void main(String[] args) throws Exception
>     {
>         ictx = new InitialContext();
>         Queue queue = (Queue) ictx.lookup("queue");
>         QueueConnetionFactory qcf = (QueueConnetionFactory) ictx.lookup("jms/__defaultConnectionFactory");
>         ictx.close();
> 
>         QueueConnection qc = qcf.createQueueConnection();
>         QueueSession qs = qc.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
>         QueueSender qrecv = qs.createReceiver(queue);
>         TextMessage msg;
> 
>         for(i=0;i<10;i++) 
>         {
>             msg = (TextMessage) qrecv.receive();//slusamo sinhrono!
>             System.out.println("Msg received: " + msg.getText());
>         }
> 
>         qc.close();
>     }
> }
> ```

## JMS Publish-Subscribe API 
Sada je sve isto, samo nije Queue nego Topic, kao što se vidi na slici:

![alt text](https://i.imgur.com/92ISWXp.png)

Producer je **TopicPublisher**, a consumer je **TopicSubscriber**.

Radnje koje bi trebalo obaviti:

- Producer: 
    - preuzmi referencu za TopicConnectionFactory od InitialContext-a
    - preuzmi referencu za Destination tj. Topic
    - kreiraj TopicConnection konekciju
    - kreiraj TopicSession sesiju
    - kreiraj TopicPublisher producera
    - kreiraj Message poruku
    - pošalji poruku 

- Consumer:
    - preuzmi referencu za TopicConenctionFactory od InitialContext-a
    - preuzmi referencu za Destination tj. Topic
    - kreiraj TopicConnection konekciju
    - kreiraj TopicSession sesiju
    - kreiraj TopicSubscriber consumera 
    - čekaj na poruku (sinhrono) ili kreiraj MessageListener (asinhrono) 

### Primer.

### Publisher.java
> ```java
> public class Publisher 
> {
>     static Context ictx = null;
> 
>     public static void main(String[] args) throws Exception
>     {
>         //standardno:
>         ictx = new InitialContext();
>         Topic topic = (Topic) ictx.lookup("topic");
>         TopicConnetionFactory tcf = (TopicConnetionFactory) ictx.lookup("jms/__defaultConnectionFactory");
>         ictx.close();
> 
>         try(TopicConnection tc = tcf.createTopicConnection())
>         {
>             TopicSession ts = tc.createTopicSession(true, Session.AUTO_ACKNOWLEDGE);
>             //true za topic sesiju!!! Do sada je uvek bilo false!
>             //zasto? Zelim da sve poruke budu poslate zajedno u istom trenutku. 
>             //ako je true, mora da se stavi ts.commit()!!!!
>             TopicPublisher tpub = ts.createPublisher(topic);
>             TextMessage msg = ts.createTextMessage();
> 
>             System.out.println("Publisher");
>             Scanner reader = new Scanner(System.in);
>             for(int i=0;i<10;i++) 
>             {
>                 String s = reader.nextLine();
>                 msg.setText(s);
>                 tpub.publish(msg);
>             }
>             ts.commit();//mora da se navede ako je u sessio stavljen true!!!!
>         }
>     }
> }
> ```

### Subscriber.java
> ```java
> public class Subscriber 
> {
>     static Context ictx = null;
> 
>     public static void main(String[] args) throws Exception
>     {
>         //standardno:
>         ictx = new InitialContext();
>         Topic topic = (Topic) ictx.lookup("topic");
>         TopicConnetionFactory tcf = (TopicConnetionFactory) ictx.lookup("jms/__defaultConnectionFactory");
>         ictx.close();
> 
>         TopicConnection tc = tcf.createTopicConnection();
>         TopicSession ts = tc.createTopicSession(true, Session.AUTO_ACKNOWLEDGE);
>         TopicSubscriber tsub = ts.createTopicSession(topic);
>         tsub.setMessageListener(new MsgListener("topic"));
>         tc.start();
>         System.out.println("Subscriber");
>         System.in.read();
>         tc.close();
>     }
> }
> ```

### MsgListener.java

> ```java
> public class MsgListener implements MessageListener 
> {
>     String id;
> 
>     public MsgListener()
>     {
>         id = "";
>     }
> 
>     public MsgListener(String id) 
>     {
>         this.id = id;
>     }
> 
>     public void onMessage(Message msg) 
>     {
>         System.out.println(id + ": Stigla poruka");
>         TextMessage tmsg = (TextMessage) msg;
> 
>         try
>         {
>             System.out.println(id + ": " + tmsg.getText());
>         }
>         catch(JMSException ex)
>         {
>             Logger.getLogger(MsgListener.class.getName()).log(Level.SEVERE, null);
>         }
>     }
> }
> ```

