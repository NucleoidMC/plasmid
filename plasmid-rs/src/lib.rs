use std::pin::Pin;

use anyhow::Error;
use async_trait::async_trait;
use bytes::Bytes;
use futures::{Sink, SinkExt, Stream, StreamExt};
use glam::{IVec3, Vec3};
use serde::{Deserialize, Serialize};
use tokio::net::{TcpListener, TcpStream};
use uuid::Uuid;
use xtra::{Actor, Address, Context, Handler, Message, spawn::TokioGlobalSpawnExt};

const MAX_FRAME_LENGTH: usize = 4 * 1024 * 1024;
const FRAME_HEADER_SIZE: usize = 4;

#[derive(Serialize, Deserialize, Debug, Clone, Copy, Eq, Hash, Ord, PartialEq, PartialOrd)]
#[serde(transparent)]
pub struct PlayerRef(Uuid);

pub type Identifier = String;

#[derive(Serialize)]
#[serde(tag = "type", content = "body")]
pub enum Command {
    #[serde(rename = "plasmid:teleport")]
    Teleport {
        player: PlayerRef,
        dest: Vec3,
    },
    #[serde(rename = "plasmid:set_block")]
    SetBlock {
        pos: IVec3,
        block: Identifier,
    },
    #[serde(rename = "plasmid:give_item")]
    GiveItem {
        player: PlayerRef,
        item: Identifier,
        quantity: u32,
    },
}

#[derive(Deserialize, Debug)]
#[serde(tag = "type", content = "body")]
pub enum Event {
    #[serde(rename = "plasmid:participants")]
    SetParticipants {
        players: Vec<PlayerRef>,
    },
}

impl Message for Event {
    type Result = ();
}

impl Message for Command {
    type Result = ();
}

pub struct Plasmid {
    tx: Pin<Box<dyn Sink<Command, Error=Error> + Send + Sync + 'static>>,
    participants: Vec<PlayerRef>,
}

#[async_trait]
impl Actor for Plasmid {
    type Stop = ();
    async fn stopped(self) {}
}

#[async_trait]
impl Handler<Command> for Plasmid {
    async fn handle(&mut self, message: Command, _ctx: &mut Context<Self>) {
        self.tx.send(message).await.expect("failed to send")
    }
}

#[async_trait]
impl Handler<Event> for Plasmid {
    async fn handle(&mut self, message: Event, _ctx: &mut Context<Self>) {
        dbg!(&message);
        match message {
            Event::SetParticipants { players } => {
                self.participants = players;
            }
        }
    }
}

pub struct GetParticipants;

impl Message for GetParticipants {
    type Result = Vec<PlayerRef>;
}

#[async_trait]
impl Handler<GetParticipants> for Plasmid {
    async fn handle(&mut self, _: GetParticipants, _ctx: &mut Context<Self>) -> Vec<PlayerRef> {
        self.participants.clone()
    }
}

impl Plasmid {
    pub async fn connect() -> Address<Self> {
        let listener = TcpListener::bind("0.0.0.0:12345").await.unwrap();
        let (tcp, _) = listener.accept().await.unwrap();

        let (tx, rx) = split_framed(tcp);

        let plasmid = Plasmid {
            tx: Box::pin(tx),
            participants: vec![],
        };

        let addr = plasmid.create(None).spawn_global();
        tokio::spawn(addr.clone().attach_stream(rx));
        addr
    }
}

fn split_framed(stream: TcpStream) -> (impl Sink<Command, Error=Error> + Send, impl Stream<Item=Event>) {
    let (sink, stream) = tokio_util::codec::LengthDelimitedCodec::builder()
        .big_endian()
        .max_frame_length(MAX_FRAME_LENGTH)
        .length_field_length(FRAME_HEADER_SIZE)
        .num_skip(FRAME_HEADER_SIZE)
        .length_field_offset(0)
        .length_adjustment(0)
        .new_framed(stream)
        .split();

    let sink = sink.with(|message: Command| async move {
        let mut bytes = Vec::with_capacity(64);
        serde_json::to_writer(&mut bytes, &message)?;
        Ok(Bytes::from(bytes))
    });

    let stream = stream.map(|result| {
        serde_json::from_slice(&result.unwrap()).unwrap()
    });

    (sink, stream)
}
