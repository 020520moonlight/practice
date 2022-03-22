const changeIcon = document.querySelector('.change');
changeIcon.addEventListener('click', function() {
    fetch('/singer/random')
      .then(function(response) {
        return response.json();
      })
      .then(function(body) {
        if (!body) {
          return;
        }

        function createItem(item) {
          const li = document.createElement('li');
          li.innerHTML = `
            <li>
                <div class="icon" style="background:url(${item.avatar}) no-repeat center;background-size: contain;">
                    <!-- 选择后，头像上有对勾 -->
                    <div class="shadow">
                        <span></span>
                    </div>
                </div>
                <h4>${item.name}</h4>
                <div class="like">喜欢</div>
            </li>`;
          return li;
        }

        const artistsList = document.querySelector('.artists-list');
        artistsList.innerHTML = '';
        for (const singerItem of body) {
          console.log(singerItem);
          const li = createItem(singerItem);
          artistsList.appendChild(li);
        }
      });
});


