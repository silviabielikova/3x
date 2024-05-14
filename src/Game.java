import java.util.*;

/**
 * Represents logics of "3x" game.
 */
public class Game {
    private final int width;
    private final int height;
    final private int totalSlots;
    private int elapsedTime = 0;
    private int score = 0;
    private int occupiedSlots;
    private final Item currentItem = new Item(Values.EMPTY);
    private final Item savedItem = new Item(Values.EMPTY);
    private Slot chosenSlot = null;
    private boolean pickedSaved = false;
    private final Slot[][] slots;

    private List<Slot> dangerousSlots = new ArrayList<>();
    private static final int[][] dirs = new int[][]{{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

    private static final Map<Values, Integer> points = new HashMap<>(Map.ofEntries(Map.entry(Values.LVL1, 10),
            Map.entry(Values.LVL2,
            20), Map.entry(Values.LVL3, 30), Map.entry(Values.LVL4, 40), Map.entry(Values.LVL5, 50)));

    private final Random random = new Random();

    /**
     * Class constructor specifying number of columns and rows in game.
     * @param width number of columns in game's grid
     * @param height number of rows in game's grid
     */
    public Game(int width, int height) {
        this.width = width;
        this.height = height;

        totalSlots = this.width * height;
        slots = createSlots();

        generateStartingBoard();
        currentItem.setValueToRandom();
    }

    private Slot[][] createSlots() {
        Slot[][] slots;
        slots = new Slot[this.height][this.width];

        for (int i = 0; i < this.width; i++) {
            for (int j = 0; j < this.height; j++) {
                slots[j][i] = new Slot(i, j, null);
            }
        }
        return slots;
    }

    private void generateStartingBoard() {
        int n = (getWidth() * getHeight()) / 3;
        int placed = 0;
        while (placed < n) {
            int x = random.nextInt(getWidth());
            int y = random.nextInt(getHeight());
            Slot slot = slots[y][x];
            if (slot.isOccupied()) {
                continue;
            }
            slots[y][x].setValue(getRandomValue());
            placed++;
        }
    }

    /**
     * Enum specifying all items assignable to slots.
     */
    public enum Values {
        EMPTY,
        DANGER,
        LVL1,
        LVL2,
        LVL3,
        LVL4,
        LVL5;

        private static final Values[] vals = values();

        /**
         * Levels up the value, used only on LVL[1-5] values.
         * @return the next level value
         */
        private Values levelUp() {
            int ord = this.ordinal();
            if (ord == 0 || ord == 1) {
                return this;
            }
            return vals[(this.ordinal() + 1) % vals.length];
        }
    }

    class ElementWithValue {
        protected Values value = Values.EMPTY;
        protected ElementWithValue(Values value) {
            if (value != null) {
                setValue(value);
            }
        }

        public Values getValue() {
            return value;
        }

        public void setValue(Values value) {
            this.value = value;
        }

        public void setValueToRandom() {
            value = getRandomValue();
        }

        /**
         * Checks if slot has any <code>value</code> other than <code>Values.EMPTY</code> assigned.
         * @return true if <code>value</code> is not <code>Values.EMPTY</code> or null
         */
        public boolean isOccupied() {
            return getValue() != Values.EMPTY && getValue() != null;
        }
    }

    class Item extends ElementWithValue{

        /**
         * Class constructor specifying assigned value
         * @param value assigned value
         */
        public Item(Values value) {
            super(value);
        }
    }

    /**
     * Represents a single slot in this game, keeps track of its assigned value, x and y coordinates.
     */
    public class Slot extends ElementWithValue{
        final private int x;
        final private int y;

        /**
         * Class constructor specifying assigned value, x and y coordinates.
         * @param x x coordinate
         * @param y y coordinate
         * @param value assigned value
         */
        public Slot(int x, int y, Values value) {
            super(value);
            this.x = x;
            this.y = y;
        }

        /**
         * Gets the x coordinate of the slot.
         * @return x coordinate
         */
        public int getX() {
            return x;
        }

        /**
         * Gets the y coordinate of the slot.
         * @return y coordinate
         */
        public int getY() {
            return y;
        }

        /**
         * Sets this slot's value and adjusts <code>occupiedSlots</code> accordingly.
         * @param value newly assigned value
         */
        public void setValue(Values value) {
            if (this.isOccupied()) {
                occupiedSlots += value != Values.EMPTY ? 0 : -1;
            }
            else {
                occupiedSlots += value != Values.EMPTY ? 1 : 0;
            }

            this.value = value;
        }

        /**
         * Levels up this slot's value.
         */
        public void levelUp() {
            setValue(getValue().levelUp());
        }
    }

    /**
     * Gets the number of columns in the game.
     * @return number of columns
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the number of rows in the game.
     * @return number of rows
     */
    public int getHeight() {
        return height;
    }

    /**
     * Gets the current score in the game.
     * @return current score
     */
    public int getScore() {
        return score;
    }

    /**
     * Gets the current item of the game.
     * @return current item
     */
    public Item getCurrentItem() {
        return currentItem;
    }

    /**
     * Gets the saved item of the game.
     * @return saved item
     */
    public Item getSavedItem() {
        return savedItem;
    }

    /**
     * Gets all the slots of the game
     * @return array of all the slots of the game
     */
    public Slot[][] getSlots() {
        return slots;
    }

    /**
     * Gets the time that has passed since the beginning of the game.
     * @return elapsed time since the start
     */
    public int getElapsedTime() {
        return elapsedTime;
    }

    /**
     * Increments the time that has passed since the beginning of the game.
     */
    public void incrementElapsedTime() {
        this.elapsedTime++;
    }

    /**
     * Makes a single step in this game -- sets chosen item's value to the value from <code>savedItem</code> or
     * <code>currentItem</code>, sets a new value for <code>currentItem</code> and updates dangerous slots, does not
     * analyze the last step or add score.
     */
    public void makeStep(){
        if (chosenSlot == null) {
            return;
        }

        Values chosenItem;
        if (isSavedPicked()) {
            chosenItem = savedItem.getValue();
            savedItem.setValue(Values.EMPTY);
            pickedSaved = false;
        }
        else {
            chosenItem = currentItem.getValue();
        }

        chosenSlot.setValue(chosenItem);
        if (chosenItem == Values.DANGER) {
            dangerousSlots.add(chosenSlot);
        }

        updateDangerSlots(chosenItem == Values.DANGER);
        currentItem.setValueToRandom();
    }

    public void chooseSlot(Slot slot) {
        chosenSlot = slot.isOccupied() ? null : slot;
    }

    /**
     * Sets <code>savedItem</code>'s value to <code>currentItem</code>'s.
     */
    public void saveItem() {
        if (currentItem.getValue() == Values.DANGER) {
            return;
        }
        if (hasSaved()) {
            return;
        }

        savedItem.setValue(currentItem.getValue());
        currentItem.setValueToRandom();
    }

    /**
     * Picks saved item, it will be used in the next step instead of current item.
     */
    public void pickSavedItem() {
        if (currentItem.getValue() == Values.DANGER) {
            return;
        }
        pickedSaved = true;
    }

    /**
     * Checks if player has any item saved.
     * @return true if there is a saved item in this game
     */

    public boolean hasSaved() {
        return savedItem.isOccupied();
    }

    /**
     * Picks current item, it will be used in the next step.
     */
    public void pickCurrentItem() {
        pickedSaved = false;
    }

    /**
     * Moves all danger values on this board randomly, each takes one step if possible, if any was placed in the last
     * step, it stays still.
     * @param dangerItemPlaced true if item placed in the last step had a danger value
     */
    private void updateDangerSlots(boolean dangerItemPlaced) {
        if (dangerousSlots.isEmpty()) {
            return;
        }

        List<Slot> newDangerSlots = new ArrayList<>();

        int n = dangerousSlots.size();
        if (dangerItemPlaced) {
            n -= 1;
            newDangerSlots.add(dangerousSlots.get(n));
        }

        for (int i = 0; i < n; i++) {
            Slot c = dangerousSlots.get(i);
            List<Slot> free = getFreeAdjacent(c);
            if (free.size() == 0) {
                newDangerSlots.add(c);
                continue;
            }

            int rnd = random.nextInt(free.size());
            Slot rndSlot = free.get(rnd);
            c.setValue(Values.EMPTY);
            rndSlot.setValue(Values.DANGER);
            newDangerSlots.add(rndSlot);
        }
        dangerousSlots = newDangerSlots;
    }

    /**
     * Finds all adjacent slots that aren't occupied.
     * @param slot central slot
     * @return list of free adjacent slots
     */
    private List<Slot> getFreeAdjacent(Slot slot) {
        List<Slot> free = new ArrayList<>();
        int myX = slot.getX();
        int myY = slot.getY();

        for (int i = 0; i < 4; i++) {
            int newX = myX - dirs[i][0];
            int newY = myY - dirs[i][1];
            if (0 <= newX && 0 <= newY && newX < this.width && newY < this.height) {
                Slot newSlot = slots[newY][newX];
                if (!newSlot.isOccupied()) {
                    free.add(newSlot);
                }
            }
        }
        return free;
    }

    /**
     * Collects scoring slots -- scoring slots are touching <code>chosenSlot</code> and their values are the same as
     * <code>chosenSlot</code>'s value.
     * @return true if player has scored in this step
     */
    public boolean checkAdjacency() {
        if (chosenSlot == null) {
            return false;
        }

        Values value = chosenSlot.getValue();
        if (value == Values.EMPTY || value == Values.DANGER) {
            return false;
        }

        int oldScore = score;

        Queue<Slot> adjacent = new LinkedList<>();
        Queue<Slot> checked = new LinkedList<>();

        adjacent.add(chosenSlot);

        while (!adjacent.isEmpty()) {
            Slot current = adjacent.poll();
            if (current.getValue() != chosenSlot.getValue()) {
                continue;
            }

            int myX = current.getX();
            int myY = current.getY();

            for (int i = 0; i < 4; i++) {
                int newX = myX - dirs[i][0];
                int newY = myY - dirs[i][1];
                if (0 <= newX && 0 <= newY && newX < this.width && newY < this.height) {
                    Slot newSlot = slots[newY][newX];
                    if (!checked.contains(newSlot) && !adjacent.contains(newSlot)) {
                        adjacent.add(newSlot);
                    }
                }
            }
            checked.add(current);
        }
        mergeValues(checked);

        return oldScore != score;
    }

    /**
     * Merges and levels up values on board after scoring.
     * @param scoringSlots queue of slots set to be merged
     */
    private void mergeValues(Queue<Slot> scoringSlots) {
        if (scoringSlots.size() >= 3) {
            Slot slot = scoringSlots.poll();
            score += points.get(slot.getValue());
            slot.levelUp();
            checkAdjacency();

            int cnt = 1;

            while (!scoringSlots.isEmpty()) {
                Slot c = scoringSlots.poll();
                cnt++;

                int increment = points.get(c.getValue());
                score += cnt > 3 ? cnt + increment : increment;

                c.setValue(Values.EMPTY);
            }
        }
    }

    /**
     * Picks random value, each value has a different adjusted probability of it being picked.
     * @return randomly picked value
     */
    public Values getRandomValue() {
        float r = random.nextFloat();

        if (score > 400) {
            if (r < 0.1) {
                return Values.DANGER;
            }
        }

        if (r < 0.5) {
            return Values.LVL1;
        }

        if (r < 0.7) {
            return Values.LVL2;
        }

        if (r < 0.8) {
            return Values.LVL3;
        }

        if (r < 0.9) {
            return Values.LVL4;
        }

        return Values.LVL5;
    }

    /**
     * Checks if player chose saved item for the next move.
     * @return true if saved item is chosen
     */
    public boolean isSavedPicked() {
        return pickedSaved;
    }

    /**
     * Checks if game has ended.
     * @return true if all slots are occupied
     */
    public boolean isOver() {
        return occupiedSlots == totalSlots;
    }
}