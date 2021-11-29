# Kalkulator - Callback

Koristeći WCF kreirajte **full-duplex** sistem kalkulatora. Korisnik u svojoj sesiji može da:
- obriše trenutno računanje, 
- doda broj, 
- oduzme broj, 
- pomnoži brojem i 
- podeli rezultat prosleđenim brojem. 
Svaka operacija se odmah izvršava nad rezultatom (prethodni rezultat) i smešta u rezultat, Operacija nema povratnu vrednost. Servis po izvršenju operacije poziva klijenta i prosleđuje mu vrednost rezultata i do tog momenta kreiranog izraza (Na primer: rezulata 7 i izraz 2+3-5*7).

Obavezno izdvojiti interfejs, implementaciju, web.config (dovoljan je samo deo za setovanje servisa i na klijentskoj strani deo za callback) i klijentsku stranu koja demonstirira rad servisa. Klijentska strana mora pozvati sve metode servisa i prikazati njihov rezultat ako postoji. 

> **ICalculator je zapravo SERVIS i njega poziva KLIJENT, a ICalcCallback poziva SERVIS da bi prosledio klijentu!!!**
> Da bi se koristio CALLBACK potrebna je **sesija**! Pa u interfejsu ICalculator mora da se navede: 
> ```csharp
> [ServiceContract(CallbackContract = typeof(ICalcCallback), SessionMode = SessionMode.Required)]
> ```
> Sve metode u tom interfejsu moraju da imaju: 
> ```csharp
> [OperationContract(IsOneWay=true)]
> ```
> Ako se traži callback to ne znači da ne može da se koristi i ne-callback način rada. Može da se vrši kombinovanje!

## ICalculator.cs 
```csharp
namespace WcfCalculator
{
    //kada postoji full-duplex u zadatku onda mora ova sledeca linija:
    [ServiceContract(CallbackContract = typeof(ICalcCallback), SessionMode = SessionMode.Required)]

    public interface ICalculator
    {
        //metode koje ne vracaju vrednost, nego koriste callback obelezavaju se sa IsOneWay=true
        [OperationContract(IsOneWay = true)]
        public void Dodaj(decimal a);

        [OperationContract(IsOneWay = true)]
        public void Oduzmi(decimal a);

        [OperationContract(IsOneWay = true)]
        public void Pomnozi(decimal a);

        [OperationContract(IsOneWay = true)]
        public void Podeli(decimal a);
    }

    [DataContract]
    public class Reuzltat
    {
        decimal vrednost = 0.0M;
        string izraz = "";

        [DataMember] 
        public decimal Vrednost { get; set; }

        [DataMember] 
        public string Izraz { get; set; }
    }
}
```

## Web.config

```xml
<system.serviceModel>
    <services>
        <service name = "WcfCalculator.Calculator">
            <endpoint
            binding="wsDualHttpBinding"
            contract="WcfCalculator.ICalc"
            address=""
            />
        </service>
    </services>
</system.serviceModel>
```

## ICalcCallback

```csharp
namespace WcfCalculator
{
    public interface ICalcCallback
    {
        [OperationContract(IsOneWay = true)]
        void Rezultat(Rezultat r);
    }
}
```

## Calculator.svc.cs - Servis

```csharp
namespace WcfCalculator
{
    //ova linija je NEOPHODNA:
    [ServiceBehavior(InstanceContentMode = InstanceContentMode.PerSession)]

    public class Calculator: ICalculator
    {
        private decimal r = 0.0M;
        private string izraz = "";

        //ako zelimo da za svakog klijenta zasebno radi kalkulator - samo sa njegovim vrednostima, kao da svako ima svoj kalkulator, onda ostaje ovako kako je
        //ako zelimo da klijenti dele jedan isti kalukulator, tj. da jedan klijent vidi racunanja drugih klijenata, onda decimal i string moraju biti static:
        //static decimal r = 0.0M;
        //static string izraz = "";

        //medjutim, nekad je neophodno da ovo radi u Single rezimu umesto u PerSession
        //ali tada callback atribut ne postoji! Prosto ne moze ovako da se pribavi!
        //onda na scenu stupa protected property Callback, samo se svuda umesto callback pise Callback i onda ce sve raditi lepo kao i ranije, a radi kao Signle :)


        private ICalcCallback callback;

        protected ICalcCallback Callback
        {
            get 
            {
                return OperationContext.Current.GetCallbackChannel<ICalcCallback>();
            }
        }

        public Calculator()
        {
            callback = OperationContext.Current.GetCallbackChannel<ICalcCallback>();
        }

        public void Dodaj(decimal a)
        {
            r += a;
            izraz += $" + {a}";

            callback.Rezultat(new Rezultat() 
            {
                Izraz = izraz, 
                Vrednost = r
            });
        }

        public void Oduzmi(decimal a)
        {
            r -= a;
            izraz += $" - {a}";

            callback.Rezultat(new Rezultat() 
            {
                Izraz = izraz, 
                Vrednost = r
            });
        }

        public void Pomnozi(decimal a)
        {
            r *= a;
            izraz += $" * {a}";

            callback.Rezultat(new Rezultat() 
            {
                Izraz = izraz, 
                Vrednost = r
            });
        }

        public void Podeli(decimal a)
        {
            if(a == 0.0M)
            {
                izraz = "Deljenje nulom!";
                r = 0.0M;
            }
            else 
            {
                r /= a;
                 izraz += $" / {a}";
            }

            callback.Rezultat(new Rezultat() 
            {
                Izraz = izraz, 
                Vrednost = r
            });
        }
    }
}
```
> * Ako želimo da za svakog klijenta zasebno radi kalkulator - samo sa njegovim vrednostima, kao da svako ima svoj kalkulator, onda ostaje ovako kako je gore u Calculator klasi. 
> 
> * Ali, ako želimo da *klijenti dele jedan isti kalukulator*, onda postoje određeni načini da se to izvede, njih navodim ispod svega :)

## Klijent.cs
```csharp
namespace Klijent
{
    public class Form1: Form, ICalcCallback
    {
        private CalcClient proxy;

        public Form1()
        {
            InitializeComponent();
            proxy = new CalcClient(new InstanceContext(this));
        }

        //implementira metod interfejsa ICalcCallback:
        public void Rezultat(Rezultat r)
        {
            txtIzraz.Text = r.Izraz;
            txtVrednost.Text = r.Vrednost.ToString();
        }

        private void btnPlus_Click(object sender, EventArgs e)
        {
            decimal d;
            if(decimal.TryParse(txtUnos.Text, out d))
            {
                proxy.Dodaj(d);
            }
            else
            {
                MessageBox.Show("greska u prevodjenju broja.");
            }
        }

        private void btnMinus_Click(object sender, EventArgs e)
        {
            decimal d;
            if(decimal.TryParse(txtUnos.Text, out d))
            {
                proxy.Oduzmi(d);
            }
            else
            {
                MessageBox.Show("greska u prevodjenju broja.");
            }
        }

        private void btnPuta_Click(object sender, EventArgs e)
        {
            decimal d;
            if(decimal.TryParse(txtUnos.Text, out d))
            {
                proxy.Pomnozi(d);
            }
            else
            {
                MessageBox.Show("greska u prevodjenju broja.");
            }
        }

        private void btnPodeljeno_Click(object sender, EventArgs e)
        {
            decimal d;
            if(decimal.TryParse(txtUnos.Text, out d))
            {
                proxy.Podeli(d);
            }
            else
            {
                MessageBox.Show("greska u prevodjenju broja.");
            }
        }
    }
}
```

 
> * Dakle, ako želimo da *klijenti dele jedan isti kalukulator, tj. da jedan klijent vidi računanja drugih klijenata*, 
> 1. Onda decimal i string moraju biti **static**:
> ```csharp
> static decimal r = 0.0M;
> static string izraz = "";
> ```
> 
> 2. Medjutim, nekad nećemo da dodajemo static, nego je neophodno da ovo radi u **Single** režimu umesto u **PerSession**. Ali tada **callback** atribut ne postoji! Prosto ne može ovako da se pribavi! Onda na scenu stupa **protected property Callback**, samo se svuda umesto **callback** piše **Callback** i onda ce sve raditi lepo kao i ranije, a radi kao Signle :)
> ```csharp
> public void Dodaj(decimal a)
> {
>       r += a;
>       izraz += $" + {a}";
>       //callback.Rezultat(new Rezultat() 
>       //umesto callback ide Callback
>       Callback.Rezultat(new Rezultat()
>       {
>           Izraz = izraz, 
>           Vrednost = r
>       });
> }
>```
> 3. Postoji još jedan način za sve ovo:
> * Pošto svako od nas kada se konektuje na servis generiše svoju sesiju i ima svoj callback, možemo da napravimo listu svih vallback-ova. Zašto? Pa zato što kada se jednom desi promena treba obavestiti sve klijente u isto vreme. Evo izmena:
> 
> ```csharp
> ...
> 
> private List<ICalcCallback> callbacks;
>
> public Calculator()
> {
>       //callback = OperationContext.Current.GetCallbackChannel<ICalcCallback>();
>       callbacks = new List<ICalcCallback>();
> }
> 
> //dodacemo funkciju za dodavanje callback-a u listu callback-ova:
> public void setCallbacks()
> {
>       var c = OperationContext.Current.GetCallbackChannel<ICalcCallback>();
>       if(!callbacks.Contains(c)) 
>       {
>           callbacks.Add(c);
>       }
> }
> protected List<ICalcCallback> Callback
> {
>       get 
>       {
>           //ovde pozivamo tu metodu:
>           setCallbacks();
>           return callbacks;
>       }
> }
> 
> public void Dodaj(decimal a)
> {
>       r += a;
>       izraz += $" + {a}";
>       //sad za svaki callback iz liste pozovemo:
>       Callback.ForEach(x=>x.Rezultat(new Rezultat() 
>       {
>           Izraz = izraz, 
>           Vrednost = r
>       });
>  }
> //i tako za svaku operaciju...
> 
> //potrebno je dodati sledecu funkciju (i u interfjesu ICaclculator)
> public void Register() 
> {
>       //samo pozovemo Callback da bise odmah na startu registrovali callback-ovi
>       //a ne da se cekaju pozivi operacija za to
>       //ovako ce odmah da se kreiraju callback-ovi u da se ubace u listu
>       setCallbacks();
> }
> ...
> ```
> Osim ovih izmena, u Klijentu moramo da promenimo samo u konstruktoru da se pozove Register funkcija cim se kreira klijent, logicno
> ```csharp
> public class Form1: Form, ICalcCallback
> {
>       ...
>       public Form1() 
>       {
>           InitializeComponent();
>           proxy = new CalcClient(new InstanceContext(this));
>           //ovo je dodatak:
>           proxy.Register();
>       }
> }
