package xyz.nucleoid.plasmid.api.game;

import net.minecraft.text.Text;

public interface GameSpaceState {
    int players();
    int spectators();
    int maxPlayers();
    State state();
    boolean canSpectate();
    boolean canPlay();

    record State(Text display, boolean hidden) {
        public static final State WAITING = new State(Text.translatable("text.plasmid.game_state.waiting"), false);
        public static final State STARTING = new State(Text.translatable("text.plasmid.game_state.starting"), false);
        public static final State ACTIVE = new State(Text.translatable("text.plasmid.game_state.active"), false);
        public static final State CLOSING = new State(Text.translatable("text.plasmid.game_state.closing"), true);
    }


    class Builder implements GameSpaceState {
        private int players = 0;
        private int spectators = 0;
        private int maxPlayers = -1;
        private State state = State.ACTIVE;
        private boolean canSpectate = true;
        private boolean canPlay = true;

        public Builder() {}

        public Builder(GameSpace space) {
            this.players = space.getPlayers().players().size();
            this.spectators = space.getPlayers().spectators().size();
        }

        public Builder players(int players, int spectators, int maxPlayers) {
            return this.players(players).spectators(spectators).maxPlayers(maxPlayers);
        }

        public Builder players(int players, int maxPlayers) {
            return this.players(players).maxPlayers(maxPlayers);
        }

        public Builder players(int players) {
            this.players = players;
            return this;
        }

        public Builder spectators(int spectators) {
            this.spectators = spectators;
            return this;
        }

        public Builder maxPlayers(int maxPlayers) {
            this.maxPlayers = maxPlayers;
            return this;
        }

        public Builder state(State state) {
            this.state = state;
            return this;
        }

        public Builder canSpectate(boolean canSpectate) {
            this.canSpectate = canSpectate;
            return this;
        }

        public Builder canPlay(boolean canPlay) {
            this.canPlay = canPlay;
            return this;
        }


        @Override
        public int players() {
            return this.players;
        }

        @Override
        public int spectators() {
            return this.spectators;
        }

        @Override
        public int maxPlayers() {
            return this.maxPlayers;
        }

        @Override
        public State state() {
            return this.state;
        }

        @Override
        public boolean canSpectate() {
            return this.canSpectate;
        }

        @Override
        public boolean canPlay() {
            return this.canPlay;
        }
    }
}
