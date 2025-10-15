//@ts-check

const statusText = document.getElementById("status-text");

fetch("https://mpc.xplate.dev/api/getRoomsAndPeopleInThem").then((res) => {
  if (res.ok) {
    res.json().then((data) => {
      //const parsed = JSON.parse(data);
      let peopleOn = 0;
      let chatsOpen = 0;
      for (const key in data) {
        peopleOn += data[key].peopleCurrently;
        chatsOpen++;
        console.log(
          `chat with id: ${key}, has ${data[key].peopleCurrently}/${data[key].maxPeople} people in it`
        );
      }
      if (statusText != null)
        statusText.innerHTML = `<i>${peopleOn} people are online in ${chatsOpen} chats.</i>`;
      else console.error("No status text!");
    });
  }
});

function verifyCapcha() {
  // @ts-ignore
  console.log(grecaptcha.getResponse());
}
