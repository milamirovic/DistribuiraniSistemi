Ako je poenta da svaki klijent ima svoj kalkulator, da ga ne dele medjusobno, onda se treba koristiti PerSession opcija. Ovo je verzija sa callback-om. 

Kada imamo _callback_ i koristimo **_PerSession_**:
* onda se u interfejsu **_IKalkulator_** pise: **[ServiceContract(CallbackContract = typeof(IKalkulatorCallback), SessionMode = SessionMode.Required)]**
* mora da se napravi interfejs **_IKalkulatorCallback_** koji ima callback metodu: **public void VratiRezultat(Rezultat r)** koja zapravo samo stampa rezultat.
* onda se u klasi **_Kalkulator_** koja implementira interfejs IKalkulator pise **[ServiceBehaviour(InstanceContentMode = InstanceContentMode.PerSession)]**
* i u toj klasi moramo da imamo ATRIBUT **IKalkulatorCallback callback**. On dobija vrednost u konstruktoru **Kaluklator() { this.callback = OperationContext.Current.GetCallbackChannel<IKalkulatorCallback>(); }**
* onda u klijent klasi (Form ili Program) moramo da imamo atribut **KalkulatorClient proxy = new KalkulatorClient(new InstanceContext(this));**
  
```csharp
namespace WcfKalkulator  
{
    [ServiceContract(CallbackContract = typeof(IKalkulatorCallback), SessionMode = SessionMode.Required)]
    public interface IKalkulator 
    {
        [OperationContract(IsOneWay = true)]
        public void Dodaj(decimal d);

        [OperationContract(IsOneWay = true)]
        public void Oduzmi(decimal d);

        [OperationContract(IsOneWay = true)]
        public void Pomnozi(decimal d);

        [OperationContract(IsOneWay = true)]
        public void Podeli(decimal d);

        [OperationContract(IsOneWay = true)]
        public void Obrisi();
    }

    [DataContract]
    public class Rezultat
    {
        string izraz = "";
        decimal vrednost = 0.0M;

        [DataMember]
        public string Izraz { get; set; }

        [DataMember]
        pubic decimal Vrednost { get; set; }
    }
}

namespace WcfKalkulator  
{
    public interface IKalkulatorCallback 
    {
        [OperationContract(IsOneWay = true)]
        public void VratiRezultat(Rezultat r);
    }
}

namespace WcfKalkulator
{
    [ServiceBehaviour(InstanceContentMode = InstanceContentMode.PerSession)]
    public class Kalkulator : IKalkulator
    {
        private decimal vrednost = 0.0M;
        private string izraz = "";
        private IKalukulatorCallback callback;

        public Kalkulator()
        {
            this.callback = OperationContext.Current.GetCallbackChannel<IKalkulatorCallback>();
        }

        public void Dodaj(decimal d)
        {
            this.vrednost += d;
            this.izraz+= " + " + d.ToString();

            this.callback.VratiRezultat(new Rezultat()
            {
                Vrednost = vrednost, Izraz = izraz
            });
        }

        public void Oduzmi(decimal d)
        {
            this.vrednost -= d;
            this.izraz += " - " + d.ToString();

            this.callback.VratiRezultat(new Rezultat()
            {
                Izraz = izraz, Vrednost = vrednost
            });
        }

        public void Pomnozi(decimal d)
        {
            this.vrednost *= d;
            this.izraz += " * " + d.ToString();

            this.callback.VratiIzraz(new Rezultat()
            {
                Vrednost = vrednost, Izraz = izraz
            });
        }

        public void Podeli(decimal d)
        {
            if(d == 0.0M)
            {
                this.vrednost = 0.0M;
                this.izraz = "";
                this.callback.VratiRezultat(new Rezultat()
                {
                    Izraz = izraz, Vrednost = vrednost
                });
            }
            else 
            {
                this.vrednost /= d;
                this.izraz += " / " + d.ToString();

                this.callback.VratiRezultat(new Rezultat()
                {
                    Vrednost = vrednost, Izraz = izraz
                });
            }
        }
    }
}

namespace WcfKlijent
{
    public class Form1 : Form, IKalkulatorCallback 
    {
        private KalkulatorClient proxy;

        public Form1()
        {
            InitializeComponent();
            proxy = new KalkulatorClient(new InstanceContext(this));
        }

        //implementiramo interfejs IKalkulatorCallback
        public void VratiRezultat(Rezultat r)
        {
            lblIzraz.Text = r.Izraz;
            lblVrednost.Text = r.Vrednost.ToString();
        }

        public void btnOperate_Click(object sender, EventArgs e)
        {
            decimal d = numericUpdDown.Value;
            string operacija = txtOperacija.Text;

            if(operacija == "+")
            {
                proxy.Dodaj(d);
            }
            else if(operacija == "-")
            {
                proxy.Oduzmi(d);
            }
            else if(operacija == "*")
            {
                proxy.Pomnizi(d);
            }
            else if(operacija == "/")
            {
                proxy.Podeli(d);
            }
            else 
            {
                MessageBox.Show("Nepostojeca operacija!")
            }
        }

        public void btnObrisi_Click(object sender, EventArgs e)
        {
            proxy.Obrisi();
        }
    }
}
```
