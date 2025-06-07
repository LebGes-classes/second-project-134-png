package storages;

public enum City {
    NURLAT("Nurlat"), KAZAN("Kazan"), CHEBOKSARI("Cheboksari"), NIZHNEcumSK("Nizhnekamsk"), MOSCOW("Moscow");
    private final String title;

    City(String title){
        this.title = title;
    }

    public static City getByTitle(String city){
        City[] cities = City.values();

        for(City c: cities) {
            if (c.title.equals(city)) {
                return c;
            }
        }

        return null;
    }

    @Override
    public String toString(){
        return title;
    }
}
