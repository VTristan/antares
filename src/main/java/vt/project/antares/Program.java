package vt.project.antares;

public class Program {

    static void main(String[] args) {


        Voiture voiture = new Voiture("porsche", "red");
        Voiture voiture2 = new Voiture("audi", "jaune");

        System.out.println(voiture);
        System.out.println(voiture2);



    }

    static class Voiture {
        public Voiture(String brand, String color) {
            this.brand = brand;
            this.color = color;
        }

        //attributs
        public String brand;
        public String color;
        //methodes


        @Override
        public String toString() {
            return "couleur : " + this.color + " , marque : " + this.brand ;
        }
    }

}
