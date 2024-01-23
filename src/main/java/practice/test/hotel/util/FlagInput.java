package practice.test.hotel.util;

import jakarta.persistence.Temporal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import practice.test.hotel.entity.Flag;


@Service
public class FlagInput {
    private final static Logger LOGGER = LoggerFactory.getLogger(FlagInput.class);

    private static Flag flag = Flag.None;

    public Flag flag() {
        LOGGER.info("Was invoked method to get flag");
        LOGGER.debug(String.valueOf(flag));
        return flag;
    }

    public void flagSet(Flag flag) {
        LOGGER.info("Was invoked method to change flag to " + flag);
       this.flag = flag;
    }


}
