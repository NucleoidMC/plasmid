use std::time::Duration;
use plasmid::Plasmid;

#[tokio::main]
async fn main() {
    let plasmid = Plasmid::connect().await;

    loop {
        std::thread::sleep(Duration::from_millis(1000));
    }
}
