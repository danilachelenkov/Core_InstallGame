import java.io.Serializable;
import java.util.UUID;

public class GameProgress implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nickname;
    private int health;
    private int weapons;
    private int lvl;
    private double distance;

    public GameProgress(String nickname, int health, int weapons, int lvl, double distance) {
        this.nickname = nickname;
        this.health = health;
        this.weapons = weapons;
        this.lvl = lvl;
        this.distance = distance;
    }

    public String getNickname() {
        return nickname;
    }

    @Override
    public String toString() {
        return "GameProgress{" +
                "nickname= " + nickname + "" +
                "health= " + health +
                ", weapons= " + weapons +
                ", lvl= " + lvl +
                ", distance= " + distance +
                '}';
    }
}
