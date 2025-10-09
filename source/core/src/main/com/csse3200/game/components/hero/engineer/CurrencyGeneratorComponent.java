package com.csse3200.game.components.hero.engineer;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.currencysystem.CurrencyManagerComponent;
import com.csse3200.game.components.currencysystem.CurrencyComponent.CurrencyType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

/**
 * A component that periodically generates currency for its owner (typically the player).
 * <p>
 * This is mainly used for summonable entities such as engineer turrets or machines
 * that produce resources over time. It checks for an {@link OwnerComponent} to find
 * the owner automatically if not specified in the constructor.
 */
public class CurrencyGeneratorComponent extends Component {
    /**
     * The entity that owns this generator (usually the player).
     */
    private Entity owner;

    /**
     * Type of currency to generate.
     */
    private final CurrencyType type;

    /**
     * Amount of currency to generate each cycle.
     */
    private final int amount;

    /**
     * Time interval (in seconds) between currency generation cycles.
     */
    private final float intervalSec;

    /**
     * Internal timer used to track when to generate the next batch.
     */
    private float timer = 0f;

    /**
     * Constructs a currency generator.
     *
     * @param owner       The owner entity who will receive the generated currency.
     *                    Can be {@code null}; if so, it will attempt to retrieve it
     *                    from an {@link OwnerComponent} during {@link #create()}.
     * @param type        The type of currency to generate.
     * @param amount      The amount of currency generated per interval.
     * @param intervalSec The interval in seconds between each generation.
     */
    public CurrencyGeneratorComponent(Entity owner, CurrencyType type, int amount, float intervalSec) {
        this.owner = owner;
        this.type = type;
        this.amount = amount;
        this.intervalSec = intervalSec;
    }

    @Override
    public void create() {
        super.create();

        // ✅ If no owner was provided, attempt to find one via the OwnerComponent
        if (owner == null) {
            OwnerComponent oc = this.entity.getComponent(OwnerComponent.class);
            if (oc != null) {
                this.owner = oc.getOwner();
            }
        }
    }

    @Override
    public void update() {
        // Increment timer based on the frame delta time
        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        timer += dt;

        // If enough time has passed, trigger currency generation
        if (timer >= intervalSec) {
            timer -= intervalSec; // Reset timer for the next interval

            // Safety check: if the owner is missing, skip generation
            if (owner == null) {
                System.out.println("[CurrencyGen] owner == null, currency generation skipped.");
                return;
            }

            // Retrieve the CurrencyManagerComponent from the owner
            CurrencyManagerComponent cm = owner.getComponent(CurrencyManagerComponent.class);
            if (cm == null) {
                System.out.println("[CurrencyGen] Owner missing CurrencyManagerComponent, generation skipped.");
                return;
            }

            // Log before and after amounts for debugging
            int before = cm.getCurrencyAmount(type);
            cm.addCurrency(type, amount); // Adds currency and automatically updates UI
            int after = cm.getCurrencyAmount(type);

            System.out.println("[CurrencyGen] +" + amount + " " + type
                    + "  | " + before + " -> " + after);
        }
    }
}


