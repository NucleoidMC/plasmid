package xyz.nucleoid.plasmid.api.menu;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * How a {@link GameMenu} decides how much room to ask for.
 *
 * <p>One field takes any of three things:
 *
 * <pre>{@code
 * "size": "fit"      // as much as the entries need, and no more
 * "size": "9:4"      // exactly this, however few entries there are
 * "size": "inherit"  // whatever the menu this was opened from got
 * }</pre>
 *
 * <p>{@link Auto#INHERIT} keeps a tree of menus from jumping about as it is walked. A category opened from a
 * menu twice its size matches it; opened on its own it is still sized to its own contents. It never makes a
 * menu smaller than it needs.
 */
public sealed interface GameMenuSizeRule {
    /**
     * A keyword or a size. The two cannot be confused, so either reads unambiguously.
     */
    Codec<GameMenuSizeRule> CODEC = Codec.either(Auto.CODEC, GameMenuSize.CODEC).xmap(
            either -> either.map(Function.identity(), Exact::new),
            rule -> rule instanceof Exact(GameMenuSize size) ? Either.right(size) : Either.left((Auto) rule)
    );

    static GameMenuSizeRule of(GameMenuSize size) {
        return new Exact(size);
    }

    static GameMenuSizeRule of(int width, int height) {
        return of(new GameMenuSize(width, height));
    }

    /**
     * The region to ask for. Still cut down afterwards if the theme cannot leave that much free.
     *
     * @param max the largest region the theme leaves free
     * @param inherited the region the opening menu got, or {@code null} if this starts a fresh chain
     */
    GameMenuSize resolve(GameMenuSize max, GameMenuLayout<?> layout, int paddingX, int paddingY, @Nullable GameMenuSize inherited);

    /**
     * The rules that work a size out rather than state one.
     */
    enum Auto implements GameMenuSizeRule, StringRepresentable {
        /**
         * As much room as the layout needs. What a menu gets without saying otherwise.
         */
        FIT("fit"),
        /**
         * The region the opening menu got, wherever that is bigger than what the layout needs.
         */
        INHERIT("inherit");

        public static final Codec<Auto> CODEC = StringRepresentable.fromEnum(Auto::values);

        private final String name;

        Auto(String name) {
            this.name = name;
        }

        @Override
        public GameMenuSize resolve(GameMenuSize max, GameMenuLayout<?> layout, int paddingX, int paddingY, @Nullable GameMenuSize inherited) {
            var own = layout.preferredSize(max, paddingX, paddingY);

            if (this == FIT || inherited == null) {
                return own;
            }

            // Per axis, so a wider opening menu widens this and a taller one heightens it, independently.
            // Neither can squeeze it below what it needs.
            return new GameMenuSize(
                    Math.max(own.width(), inherited.width()),
                    Math.max(own.height(), inherited.height()));
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    /**
     * A region stated outright, kept whatever the menu currently holds.
     */
    record Exact(GameMenuSize size) implements GameMenuSizeRule {
        @Override
        public GameMenuSize resolve(GameMenuSize max, GameMenuLayout<?> layout, int paddingX, int paddingY, @Nullable GameMenuSize inherited) {
            return this.size;
        }
    }
}
