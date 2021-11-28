# WCF - Windows Communication Foundation

[ISKLJUCIVO PRIPREMA ZA PISMENI DEO ISPITA - NA PAPIRU]

Ovo nije bitno za zadatke:
* Zvanično, WCF je interoperabilan - servis mora da bude na Windows-u, a klijenti mogu biti na bilo kom drugom sistemu. Jedini zahtev je da servis bude na windows-u.
* Servis je klasa generisana u C# koja implementira jednu ili više metoda.
* Host je proces u kojem servis radi. 
* WCF može biti na 1 ili više end-pointa koji omogućavaju klijentima pristup servisu. 
* Klijent ne može dirkento da poziva servis, mora preko end-pointa. 


## WCF Servis 

To je klasa koja mora da ima **ServiceContract** description. Svaka metoda u okviru nje mora iznad sebe da ima definisan **OperationContract** description. Svaka metoda mora da bude **public**!

```csharp
using System.ServiceModel;

[ServiceContract]
class ImeKlase
{
    [OperationContract]
    public void NekaMetoda(...)
    {
        ...
    }
}
```

Uobičajeno je da umesto 1 klase imamo izdvojen interfejs i klasu koja implementira taj interfejs. Interfejs mora biti "ukrašen" sa **ServiceContract**, a metode u okviru njega sa **OperationContract**. Klasa koja implementira interfejs, ne mora da sadrži deskriptore jer to nasledjuje od interfejsa. 

```csharp
using System.ServiceModel;

[ServiceContract]
public interface INazivInterfejsa
{
    [OperationContract]
    bool Check(object data);
}

public class NazivKlase : INazivInterfejsa
{
    public bool Check(object data)
    {
        ...
    }
}
```

Ako imamo kompozitni objekat koji treba da prenesemo, onda on mora biti ukrašen sa **DataContract**. Svi propertiji su ukrašeni sa **DataMember**. Propertiji moraju biti javni! DataMember se koristi isključivo za javne propertije, nikako za field-ove. 

```csharp
[DataContract]
public class CekaonicaStatus
{
    [DataMember]
    public Guid LekarUiD { get; set; }
    [DataMember] 
    public string Pacijent { get; set; }
    [DataMember]
    public Status Status { get; set; }
}
```

Drugi deo za definisanje WCF-a je ***definisnje endpoint-a***. To radimo kroz konfiguraciju tj. fajl **Web.config**. Na ispitu je potrebno znati gde se šta piše u samoj konfiguracije. **Contract** deo endpoint-a je obavezan i tu se navodi klasa ili interfejs gde je definisan [ServiceContract]. Adresu možemo ostaviti prazno i to ne znamo unapred. Binding može imati 2 vrednosti - ***BasicHttpBinding*** i ***WSDualHttpBinding*** (kada imamo callback protokol).

```html
<configuration>
    <system.serviceModel>
        <services>
            <service type="NazivKlase.NazivApp">
                <endpoint 
                contract="INazivInterfejsa"
                binding="basicHttpBinding"
                address="http://hostSajt/implementacijaServisa.svc"/>
            </service>
        </services>
    </system.serviceModel>
</configuration>
```

Kada podesimo interfejs i uradimo njegovu implementaciju i podesimo Web.config, možemo da kreiramo klijenta:

# WCF Klijent

Naziv klase se uobičajeno zove ***NazivKlaseClient***. Uobičajeno da se objekti te klase zovu ***proxy***. Zašto? Jer ne vrši obradu, već samo premošćava 2 strane. Ta klasa ne sadrži ni jednu metodu ili obradu, već direktuje naše pozive ka servisu i prepakuje podatke da se predaju nama. 

Postoje sinhrone i asinhrone metode: *NazivMetode*() i *NazivMetodeAsync*().

# Primer 1. WCF TacnoVreme - najprostiji primer

* ITacnoVreme.cs
* TacnoVreme.svc.cs - servis

## ITacnoVreme.cs
```csharp
namespace WcfTacnoVreme
{
    [ServiceContract] 
    public interface ITacnoVreme
    {
        [OperationContract]
        DateTime GetVreme();

        [OperationContract]
        PristupLog KadJeBioZadnjiPristup();
    }

    [DataContract]
    public class PristupLog
    {
        [DataMember]
        public DateTime TimeStamp { get; set; }

        [DataMember]
        public string HostName { get; set; }
    }
}
```

## TacnoVreme.svc.cs
```csharp
namespace WcfTacnoVreme
{
    [ServiceBehavior(InstanceContextMode = InstanceContextMode.PerCall)]
    //PerCall - instanca ove klase se kreira svaki put kada udaljeno pristupimo ovoj klasi
    //PerSession - ako zelimo za svakog klijenta posebno stanje, da instanca klase zivi koliko zivi jedna sesija, onda cemo setovati PerSession
    public class TacnoVreme : ITacnoVreme
    {
        public DateTime GetVreme()
        {
            pristupio = DateTime.Now();
            return DateTime.Now();
        }

        DateTime pristupio = DateTime.Now();

        public PristupLog KadJeBioZadnjiPristup()
        {
            return new PristupLog()
            {
                HostName = Environment.MachineName,
                TimeStamp = pristupio
            };
        }
    }
}
```
### Bitno:

* **PerCall** - instanca ove klase (servisa) se kreira svaki put kada udaljeno pristupimo ovoj klasi. U prevodu, svaki klijent koji klikne na opciju da vidi vreme poslednjeg pristupa opciji GetVreme dobiće trenutnu vrednost vremena, a ne poslednjeg pristupa. Jer se instanca kreira svaki put kada pristupimo toj klasi, a ne jednom na početku.
* **PerSession** - svaki put kada na klijentskoj strani kažemo *proxy = new ImeKlaseClient()* tj. "napravi mi jednog klijenta" napraviće se instanca i na serverskoj strani. Instanca traje koliko i sesija. Sada se instanca klase TacnoVreme kreirala samo 1 za svakog klijenta. U prevodu, sada kada pokrenemo aplikaciju, i napravimo 2 ili više klijenata, poslednji pristup se računa za svakog klijenta na osnovu poslednjeg puta kada je klijent trazio GetVreme.
* **Single** - TacnoVreme ce biti generisano samo jednom i život te instance će biti kompletno vreme trajanja servisa. Koliko god puta da pozove metodu, radiće sa 1 instancom i šta se dešava: ako imamo 3 klijenta i kad jedan klikne ne opciju za prikaz vremena, svima će to biti prikazano kao poslednje vreme pristupa.

## Web.config
Ovoliko treba da se zna za ispit:
```xml
<configuration>
    ...
    <system.serviceModel>
        <services>
            <service type="WcfTacnoVreme.ITacnoVreme">
                <endpoint 
                contract="WcfTacnoVreme.ITacnoVreme" 
                binding="basicHttpBinding"
                address=""
                >
            </service>
        </services>
        ...
    </system.serviceModel>
</configuration>
```

## Klijent.cs

```csharp
namespace WcfTacnoVreme 
{
    public class Form1: Form
    {
        TacnoVremeClinet proxy;

        public Form1()
        {
            ...
            proxy = new TacnoVremeClinet();
            proxy.GetVremeCompleted += Proxy_VratiVremeComplited;
        }

        private void btnProveri_Click(object sender, Eventargs e)
        {
            txtVreme.Text = proxy.GetVreme().ToString();
        }
    }
}
```

# Primer 2. WCF Kalkulator

```csharp
namespace WcfKalkulator 
{
    [ServiceContract]
    {
        public interface IKalkulator
        {
            [OperationContract]
            public decimal ObaviOperaciju(decimal op1, decimal op2, string op);
        }
    }
}
```

```csharp
namespace WcfKalkulator
{
    public class Kalkulator: IKalkulator
    {
        public decimal ObaviOperaciju(decimal op1, decimal op2, string op)
        {
            switch(op)
            {
                case "+": return op1 + op2;
                case "-": return op1 - op2;
                case "*": return op1 * op2;
                case "/": 
                    if(op2 == 0) return new Exception("deljenje nulom?");
                    else return op1 / op2;
                default: throw new Exception("nepoznata operacija")
            }
        }
    }
}
```

```xml
<configuration>
    ...
    <system.serviceModel>
        <services>
            <service type="WcfKalkulator.IKalkulator">
                <endpoint 
                contract="IKalkulator" 
                binding="basicHttpBinding"
                address=""
                >
            </service>
        </services>
        ...
    </system.serviceModel>
</configuration>
```

```csharp
namespace WcfKalkulator 
{
    public class Form1: Form
    {
        KalkulatorClient proxy;

        public Form1()
        {
            ...
            proxy = new KalkulatorClient();
            ...
        }

        private void btnOperacijaPlus_Click(object sender, Eventargs e)
        {
            txtOperacijaPlus.Text = proxy.ObaviOperaciju(10.2M, 15.6M, "+").ToString();
        }

        private void btnOperacijaMinus_Click(object sender, Eventargs e)
        {
            txtOperacijaMinus.Text = proxy.ObaviOperaciju(10.2M, 15.6M, "-").ToString();
        }

        private void btnOperacijaPuta_Click(object sender, Eventargs e)
        {
            txtOperacijaPuta.Text = proxy.ObaviOperaciju(10.2M, 15.6M, "*").ToString();
        }

        private void btnOperacijaDeljenje_Click(object sender, Eventargs e)
        {
            decimal a = 10;
            decimal b = 0;//javice se exception
            txtOperacijaDeljenje.Text = proxy.ObaviOperaciju(a, b, "/").ToString();
        }
    }
}
```

# Primer 3. Cisterna 

Koristeći WCF kreirajte sistem za rad sa cisternom. Potrebno je podržati mogućnost:
- dodavanja određenog materijala koji je opisan zapreminom koja je dodata i svojom gustinom.
- ispusti određenu zapreminu.
- prikazati trenutno zauzeće cisterne i njenu gustinu.
- izlistati sve promene nad cisternom.
Obavezno izdvojiti interfejs, implementaciju, web.config (dovoljan je samo deo za setovanje servisa) i klijentsku stranu koja demonstirira rad servisa. 

## ICisterna.cs
```csharp
[ServiceContract]
public interface ICisterna
{
    [OperationContract]
    void Dodaj(Materijal m);

    [OperationContract]
    void Ispusti(float zapremina);

    [OperationContract]
    Materijal PrikaziZauzece();

    [OperationContract]
    List<string> IzlistajSvePromene();
}
```

```csharp
[DataContract]
public class Materijal 
{
    [DataMember]
    public string Naziv { get; set; }

    [DataMember]
    public float Zapremina { get; set; }

    [DataMember]
    public float Gustina { get; set; }
}
```

## Cisterna.cs

```csharp
public class Cisterna : ICisterna
{
    public void Dodaj(Materijal m)
    {
        //dodavanja određenog materijala koji je opisan zapreminom koja je dodata i svojom gustinom
        //koristi se Repository da bi se sacuvale sve promene izmedju nasih poziva
        float masa = Repository.Instance.Gustina * Repository.Instance.Zapremina + m.Gustina * m.Zapremina;
        Repository.Instance.Zapremina += m.Zapremina;
        Repository.Instance.Gustina = masa / Repository.Instance.Zapremina;
        Repository.Instance.Promene.Add($"{DateTime.Now} :: Dodata je materijal {m.Naziv} sa gustinom = {m.Gustina} zapremine {m.Zapremina}");
    }

    public void Ispusti(flot zapremina)
    {
        //ispusti odredjenu zapreminu
        if(Repository.Instance.Zapremina > zapremina)
        {
            Repository.Instance.Zapremina -= zapremina;
            Repository.Instance.Promene.Add($"{DateTime.now} :: Ispusteno je {zapremina} tecnosti, nova zapmremina je {Repository.Instance.Zapremina}.");
        }
        else
        {
            Repository.Instance.Zapremina = 0;
            Repository.Instance.Promene.Add($"{DateTime.Now} :: Cisterna je prazna.");
        }
    }

    public Materijal PreuzmiStanje()
    {
        //prikazati trenutno zauzeće cisterne i njenu gustinu
        return new Materijal()
        {
            Zapremina = Repository.Instance.Zapremina,
            Gustina = Repository.Instance.Gustina
        };
    }

    public List<string> IzlistajSvePromene()
    {
        return Repository.Instance.Promene;
    }
}
```
Moglo je i bez Repository, da se stavi gore Single. Ne znam zašto je ovo ovako samo dodatno zakomplikovano...
## Repository.cs

Čuva trenutno stanje naše cisterne. Simulira bazu podataka. 

```csharp
public class Repository 
{
    private static Repository instance;//staticki instancu na samog sebe
    private static object locker = true;

    //property koji omogucava poziv konstruktora, jer konstruktor je protected, niko od spolja ne moze da napravi instancu te klase
    //ovo je singleton zapravo
    public static Repository Instance  
    {
        get 
        {
            lock(locker) 
            {
                if(instance == null) 
                {
                    instance = new Repository();
                }
            }
            return instance;
        }
    }

    public float Zapremina { get; set; }

    public float Gustina { get; set; }

    public List<string> Promene { get; set; }

    protected Repository() //podrazumevani konstruktor
    {
        Zapremina = 0;
        Gustina = 0;
        Promene = new List<string>();
    }
}
```

## Form.cs 
```csharp
...
private void btnDodaj_Click(object sender, EventArgs e)
{
    Materijal m = new Materijal()
    {
        Naziv = txtNaziv.Text;
        Gusitna = float.Parse(txtGustina.Text);
        Zapremina = float.Parse(txtZapremina.Text);
    };
    proxy.Dodaj(m);
}

private void btnIspusti_Click(object sender, EventArgs e)
{
    proxy.Ispusti(float.Parse(txtIspustin.Text));
}

private void btnPreuzmiStanje_Click(object sender, Eventargs e)
{
    Materijal m = proxy.PreuzmiStanje();
    txtGustina.Text = m.Gustina.ToString("0.00");
    txtZapremina.Text = m.Zapremina.ToString("0.00");
}

private void btnPromene_Click(object sender, Eventargs e)
{
    List<string> lista = proxy.Promene();
    string listaTxt = "";
    for(int i=0;i<lista.Count();i++)
    {
        listaTxt += lista[i];
        listaTxt += "\n";
    }
    txtPromene.Text = listaTxt;
}
...
```
