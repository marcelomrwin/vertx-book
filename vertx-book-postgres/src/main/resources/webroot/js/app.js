function init() {
  registerHandler();
}

let eventBus;

function registerHandler() {
  eventBus = new EventBus('/eventbus');
  eventBus.onopen = function () {
    eventBus.registerHandler('out', function (error, message) {      
      updateTableContent(message.body);
    });
  }
}

function increment() {
  eventBus.send('in')
}

function url(s) {
    var l = window.location;
    return ((l.protocol === "https:") ? "wss://" : "ws://") + l.host + l.pathname + s;
}

function updateTableContent(book){
  $("<tr><td>" + book.id + "</td><td>" + book.title + "</td><td>" +
      "<a href='" + book.url + "'>" + book.url + "</a></td>" +
      "<td>" + book.author + "</td>" +
      "<td>" +
      "<button data-action='edit' class='btn btn-primary btn-sm product-edit' " +
      "data-toggle='modal' " +
      "data-target='#productModal' " +
      "data-title='" + book.title + "' " +
      "data-url='" + book.url + "' " +
      "data-author='" + book.author + "' " +
      "data-id='" + book.id + "'>" +
      "<span class='glyphicon glyphicon-pencil'></span>" +
      "</button>" +
      "&nbsp;" +
      "<button class='btn btn-danger btn-sm product-delete' data-id='" + book.id + "'>" +
      "   <span class='glyphicon glyphicon-minus'></span>" +
      "</button>" +
      "</td>" +
      "</tr>").appendTo("#content");
}
