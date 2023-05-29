public class Main {
    public static void main(String[] args) {
        String pathToInstall = "C:\\Programming\\Netology\\Test";
        String[] nicknames = {"Ivan234", "Semen_win", "Grandma"};

        Game game = new Game(nicknames.length, pathToInstall);

        if (game.install()) {
            System.out.println("Установка выполнена успешно");
        } else {
            System.out.println("Ошибка установки, разберите протокол и попробуйте еще раз!");
        }

        game.start(nicknames);
        game.addToArchive("", "", true);

        game.extractArchive("C:\\Programming\\Netology\\Test\\Games\\savegame\\archive.zip", "");


    }
}