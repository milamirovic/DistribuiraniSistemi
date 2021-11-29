```csharp
namespace WcfRegistracijaVozila
{
    [ServiceContract]
    public interface IRegistracijaVozila
    {
        [OperationContract]
        public string RegistrujVozilo(Vlasnik vlasnik, Vozilo vozilo);

        [OperationContract]
        public List<Vozilo> VratiListuVozilaVlasnika(Vlasnik vlasnik);

        [OperationContract]
        public List<Vlasnik> VratiListuSvihVlasnikaDatogModela(String model);

        [OperationContract]
        public Dictionary<Vozilo, Vlasnik> VratiListuSvihVozilaINjihovihVlasnika();
    }

    [DataContract]
    public class Vlasnik
    {
        string ime;
        string prezime;
        string jmbg;

        [DataMember]
        public string Ime{ get; set; }

        [DataMember]
        public string Jmbg{ get; set; }

        [DataMember]
        public string Prezime{ get; set; }

    }

    [DataMember]
    public class Vozilo
    {
        int id;
        string marka;
        string model;
        string boja;

        public int Id { get; set; }
        public string Marka { get; set; }
        public string Model { get; set; }
        public string Boja { get; set; }
    }
}

namespace WcfRegistracijaVozila
{
    [ServiceBehavior(InstanceContextMode = InstanceContextMode.PerSession)]
    public class RegistracijaVozila: IRegistracijaVozila
    {
        public void RegistrujVozilo(Vlasnik vlasnik, Vozilo vozilo)
        {
            //vozilo se registruje na jednog vlasnika, koliko ja znam :D
            //vlasnik moze da registruje vise vozila 

            //da li ovaj vlasnik vec postoji u bazi i to sa ovim vozilom?
            foreach(KeyValuePair<Vozilo, Vlasnik> entry in Repository.BazaVozilaIVlasnika)
            {
                if((entry.Value.Jmbg == vlasnik.Jmbg) && (entry.Key.Id == vozilo.Id))
                {
                    //vlasnik je vec registrovao dato vozilo!
                    return "Vlasnik je vec registrovao dato vozilo";
                }
            }
            //ako nije registrovao, onda ga dodajemo u bazu
            Repository.BazaVozilaIVlasnika.Add(vozilo, vlasnik);
            return "Vlasnik je uspesno registrovao vozilo!";
        }

        public List<Vozilo> VratiListuVozilaVlasnika(Vlasnik vlasnik)
        {
            //sva vozila zadatog vlasnika
            List<Vozilo> vozila = new List<Vozilo>();
            forach(KeyValuePair<Vozilo, Vlasnik> entry in Repository.BazaVozilaIVlasnika)
            {
                if(entry.Value.Jmbg == vlasnik.Jmbg)
                {
                    vozila.Add(entry.Key);
                }
            }
            return vozila;
        }

        public List<Vlasnik> VratiListuSvihVlasnikaDatogModela(String model)
        {
            //lista svih vlasnika zadatog MODELA
            List<Vlasnik> vlasnici = new List<Vlasnik>();
            foreach(KeyValuePair<Vozilo, Vlasnik> entry in Repository.BazaVozilaIVlasnika)
            {
                if(entry.Key.Model == model)
                {
                    vlasnici.Add(entry.Value);
                }
            }
            return vlasnici;
        }

        public Dictionary<Vozilo, Vlasnik> VratiListuSvihVozilaINjihovihVlasnika()
        {
            //lista svih vozila i njihovih vlasnika
            return Repository.BazaVozilaIVlasnika;
        }
    }
}

namespace Klijent
{
    public class Form1 : Form
    {
        private RegistracijaVozilaClient proxy; 

        public Form1()
        {
            InitializeComponent();

            proxy = new RegistracijaVozilaClient();
        }

        private void btnRegistruj_Click(object sender, EventArgs e)
        {
            int id;

            if(int.TryParse(txtIdVozila.Text, out id))
            {
                string ime, rezime, jmbg, marka, model, boja;

                ime = txtImeVlasnika.Text;
                prezime = txtPrezimeVlasnika.Text;
                jmbg = txtJmbgVlasnika.Text;
                marka = txtMarkaVozila.Text;
                model = txtModelVozila.Text;
                boja = txtBojaVozila.Text;

                String odgovor = proxy.RegistrujVozilo(new Vlasnik({
                    Ime = ime,
                    Prezime = prezime,
                    Jmbg = jmbg
                }), 
                new Vozilo({
                    Id = id, 
                    Marka = marka,
                    Model = model,
                    Boja = boja
                }));

                lblOdgovor.Text = odgovor;
            }
            else 
            {
                MessageBox.Show("Pogresan unos id-a vozila!");
            }
        }

        private void btnSvaVozilaVlasnika_Click(object sender, EventArgs e)
        {
            string ime, prezime, jmbg;
            ime = txtImeVlasnika1.Text;
            prezime = txtPrezimeVlasnika1.Text;
            jmbg = txtJmbgVlasnika1.Text;

            List<Vozila> lista = proxy.VratiListuVozilaVlasnika(new Vlasnik({
                Ime = ime, 
                Prezime = prezime, 
                Jmbg = jmbg
            }));

            String zaPrikaz = "Vozila vlasnika " + ime + " " + prezime + "[" + jmbg + "] su:\n";
            foreach(Vozilo v in lista)
            {
                zaPrikaz += "- Vozilo marke " + v.Marka + ", modela " + v.Model + " i boje " + v.Boja + "\n"; 
            }

            lblPrikaz.Text = zaPrikaz;
        }

        private void btnSviVlasniciModela_Click(object sender, EventArgs e)
        {
            string model = txtImeModela.Text;

            List<Vlasnik> vlasnici = proxy.VratiListuSvihVlasnikaDatogModela(model);

            String zaPrikaz = "";
            if(vlasnici.Count > 0)
            {
                zaPrikaz = "Vlasnici vozila modela " + model " su:\n";
                foreach(Vlasnik v in vlasnici)
                {
                    zaPrikaz += "- " + v.Ime + " " + v.Prezime + " ["+ v.Jmbg + "]\n";
                }
            }
            else 
            {
                zaPrikaz = "Nema vlasnika vozila datog modela!";
            }

            lblZaPrikazVlasnika.Text = zaPrikaz;
        }

        private void btnSvaVozilaIVlasnici_Click(object sender, EventArgs e)
        {
            Dictionary<Vozilo, Vlasnik> dict = proxy.VratiListuSvihVozilaINjihovihVlasnika();

            String zaPrikaz = "";

            if(dict.Count > 0)
            {
                zaPrikaz = "Lista svih vozila i njihovih vlasnika: \n";
                foreach(KeyValuePair<Vozilo, Vlasnik> entry in dict)
                {
                    zaPrikaz += "Vozilo [" + entry.Key.Id + ", " + entry.Key.Marka + ", " + entry.Key.Model + ", " + entry.Key.Boja + "] ima vlasnika [" + entry.Value.Ime + ", " + entry.Value.Prezime + ", " + entry.Value.Jmbg + "]\n";
                }
            }
            else
            {
                zaPrikaz = "Nema vozila u bazi!";
            }

            lblSve.Text = zaPrikaz;
        }
    }
}

namespace WcfRegistracijaVozila
{
    public class Repository 
    {
        private static Repository instance;
        private static object locker = true;

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

        public Dictionary<Vozilo, Vlasnik> BazaVozilaIVlasnika { get; set; }

        protected Repository()
        {
            BazaVozilaIVlasnika = new Dictionary<Vozilo, Vlasnik>();
        }
    }
}
```

```xml
<system.serviceModel>
    <services>
        <service name = "WcfRegistracijaVozila.RegistracijaVozila">
            <endpoint
                binding = "basicHttpBinding"
                contract = "IRegistracijaVozila"
                address = ""
            />
        </service>
    </services>
</system.serviceModel>
```
